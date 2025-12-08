package com.project.reach.data.respository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import com.project.reach.domain.contracts.IFileRepository
import com.project.reach.util.debug
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.OutputStream

class FileRepository(private val context: Context): IFileRepository {
    override suspend fun useFileInputStream(
        uri: Uri,
        callback: suspend (InputStream) -> Unit
    ) {
        context.contentResolver.openInputStream(uri).use { stream ->
            if (stream != null) callback(stream)
        }
    }

    override suspend fun useFileOutputStream(
        filename: String,
        callback: suspend (fileUri: Uri, outputStream: OutputStream) -> Unit
    ) {
        context.openFileOutput(filename, Context.MODE_PRIVATE).use { stream ->
            callback(filename.toUri(), stream)
        }
    }

    override fun getFileSize(uri: Uri): Long {
        return context.getFileSize(uri)
    }

    override fun getFilename(uri: Uri): String {
        return context.getFilename(uri)
    }

    override fun getMimeType(uri: Uri): String {
        return context.getMimeType(uri)
    }

    override fun getFileDestination(filename: String): Uri {
        return filename.toUri()
    }

    private fun Context.getFileSize(uri: Uri): Long {
        val cursor = contentResolver.query(uri, null, null, null, null)
        var size: Long = -1
        cursor?.use {
            if (it.moveToFirst()) {
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                size = it.getLong(sizeIndex)
            }
        }

        // fallback in case OpenableColumns does not return expected result
        if (size <= 0) {
            try {
                contentResolver.openFileDescriptor(uri, "r")?.use { fd ->
                    size = fd.statSize
                }
            } catch (e: FileNotFoundException) {
                debug("Error getting file size: couldn't locate file")
                throw e
            }
        }

        return size
    }

    private fun Context.getFilename(uri: Uri): String {
        return contentResolver
            .query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            } ?: uri.path?.substringAfterLast('/') ?: "unknown"
    }

    private fun Context.getMimeType(uri: Uri): String {
        val mimeType = contentResolver.getType(uri)
        if (mimeType != null) return mimeType

        val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"
    }
}