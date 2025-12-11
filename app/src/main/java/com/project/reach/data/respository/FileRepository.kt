package com.project.reach.data.respository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.project.reach.data.utils.PrivateFile
import com.project.reach.domain.contracts.IFileRepository
import com.project.reach.domain.models.MessageState
import com.project.reach.domain.models.TransferState
import com.project.reach.util.debug
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

class FileRepository(private val context: Context): IFileRepository {
    private val progressMap = ConcurrentHashMap<String, MutableStateFlow<TransferState>>()
    private val activeTransfers = MutableStateFlow<Set<String>>(setOf())

    override suspend fun useFileInputStream(
        uri: String,
        callback: suspend (InputStream) -> Unit
    ) {
        val file = File(context.filesDir, uri)
        FileInputStream(file).use { stream ->
            callback(stream)
        }
    }

    override suspend fun useFileOutputStream(
        fileHash: String,
        callback: suspend (outputStream: OutputStream) -> Unit
    ) {
        context.openFileOutput(fileHash, Context.MODE_PRIVATE).use { stream ->
            callback(stream)
        }
    }

    override fun getDownloadLocation(fileHash: String): String {
        return fileHash
    }

    override suspend fun saveFileToPrivateStorage(
        uri: Uri,
        onProgress: (progress: Long) -> Unit
    ): PrivateFile {
        return context.saveFileToPrivateStorage(uri, onProgress)
    }

    override fun getContentUri(relativePath: String): Uri {
        val authority = "${context.packageName}.provider"
        val file = File(context.filesDir, relativePath)
        return FileProvider.getUriForFile(context, authority, file)
    }

    override suspend fun updateFileTransferProgress(fileHash: String, bytesRead: Long) {
        val progress = TransferState.Progress(bytesRead)
        val state = progressMap.getOrPut(fileHash) {
            activeTransfers.update { it + fileHash }
            MutableStateFlow(progress)
        }
        state.update { progress }
    }

    override suspend fun markAsNotInProgress(fileHash: String) {
        progressMap.remove(fileHash)
        activeTransfers.update { it - fileHash }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeTransferState(
        fileHash: String,
        fileSize: Long,
        messageState: MessageState
    ): Flow<TransferState> {
        if (messageState == MessageState.DELIVERED) {
            return flowOf(TransferState.Complete)
        }

        if (messageState == MessageState.PAUSED) {
            return flowOf(TransferState.Paused)
        }

        return activeTransfers
            .map { fileHash in it }
            .distinctUntilChanged()
            .flatMapLatest { isActive ->
                if (isActive) {
                    progressMap[fileHash] ?: flowOf(TransferState.Preparing)
                } else {
                    flowOf(TransferState.Preparing)
                }
            }.flowOn(Dispatchers.IO)
    }

    override fun getFileSize(relativePath: String): Long {
        val file = File(context.filesDir, relativePath)
        return file.length()
    }

    override fun isTransferOngoing(fileHash: String): Boolean {
        return progressMap.containsKey(fileHash)
    }

    private fun Context.getFilenameFromContentUri(uri: Uri): String {
        return contentResolver
            .query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            }
            ?: uri.path?.substringAfterLast('/')
            ?: "unknown"
    }

    private fun Context.getMimeTypeFromContentUri(uri: Uri): String {
        val mimeType = contentResolver.getType(uri)
        if (mimeType != null) return mimeType

        val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            ?: "application/octet-stream"
    }

    private suspend fun Context.saveFileToPrivateStorage(
        contentUri: Uri,
        onProgress: (Long) -> Unit
    ): PrivateFile = withContext(Dispatchers.IO) {
        val digest = MessageDigest.getInstance("SHA-256")
        val tempFile = File.createTempFile("temp", ".tmp", cacheDir)
        val lastUpdateTimestamp = 0L

        FileOutputStream(tempFile).use { outputStream ->
            contentResolver.openInputStream(contentUri)?.use { inputStream ->
                val buffer = ByteArray(8192)
                var bytesRead = 0
                var totalBytesRead = 0L
                onProgress(0)

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    digest.update(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead

                    val time = System.currentTimeMillis()
                    if (time - lastUpdateTimestamp >= THROTTLE_TIME) {
                        onProgress(totalBytesRead)
                    }
                }
            }
        }

        val hashString = digest.digest().toHexString()

        val targetFile = File(filesDir, hashString)
        if (targetFile.exists()) {
            debug("File already exists")
            tempFile.delete()
        } else {
            val result = tempFile.renameTo(targetFile)
            if (!result) {
                // fallback
                tempFile.copyTo(targetFile, overwrite = true)
                tempFile.delete()
            }
        }

        val mimeType = context.getMimeTypeFromContentUri(contentUri)
        val filename = context.getFilenameFromContentUri(contentUri)

        /**
         * Anti-Corruption Layer
         *
         * Prevents unsafe access of `content://` urls
         */
        val result = PrivateFile(hashString, targetFile, mimeType, filename, hashString)
        return@withContext result
    }

    private companion object {
        const val THROTTLE_TIME = 200
    }
}