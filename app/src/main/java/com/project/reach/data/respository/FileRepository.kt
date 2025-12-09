package com.project.reach.data.respository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.project.reach.data.utils.IngestResult
import com.project.reach.domain.contracts.IFileRepository
import com.project.reach.util.debug
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest

class FileRepository(private val context: Context): IFileRepository {
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

    override fun getDownloadLocation(filename: String): String {
        return filename
    }

    override suspend fun saveFileToPrivateStorage(uri: Uri): IngestResult {
        return context.saveFileToPrivateStorage(uri)
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
        uri: Uri,
    ): IngestResult = withContext(Dispatchers.IO) {
        val digest = MessageDigest.getInstance("SHA-256")
        val tempFile = File.createTempFile("temp", ".tmp", cacheDir)

        FileOutputStream(tempFile).use { outputStream ->
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val buffer = ByteArray(8192)
                var bytesRead = 0

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    digest.update(buffer, 0, bytesRead)
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

        val mimeType = context.getMimeTypeFromContentUri(uri)
        val filename = context.getFilenameFromContentUri(uri)

        /**
         * Anti-Corruption Layer
         *
         * Prevents unsafe access of `content://` urls
         */
        val result = IngestResult(hashString, targetFile, mimeType, filename, hashString)
        return@withContext result
    }
}