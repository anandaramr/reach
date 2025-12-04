package com.project.reach.data.respository

import android.content.Context
import com.project.reach.domain.contracts.IFileRepository
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class FileRepository(private val context: Context): IFileRepository {
    override suspend fun useFileInputStream(
        filename: String,
        callback: suspend (InputStream) -> Unit
    ) {
        context.openFileInput(filename).use { stream ->
            callback(stream)
        }
    }

    override suspend fun useFileOutputStream(
        filename: String,
        callback: suspend (OutputStream) -> Unit
    ) {
        context.openFileOutput(filename, Context.MODE_PRIVATE).use { stream ->
            callback(stream)
        }
    }

    override fun getFileSize(filename: String): Long {
        val file = File(context.filesDir, filename)
        return file.length()
    }
}