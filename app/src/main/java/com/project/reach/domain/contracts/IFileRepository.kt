package com.project.reach.domain.contracts

import android.net.Uri
import java.io.InputStream
import java.io.OutputStream

interface IFileRepository {
    suspend fun useFileInputStream(uri: Uri, callback: suspend (inputStream: InputStream) -> Unit)
    suspend fun useFileOutputStream(filename: String, callback: suspend (Uri, OutputStream) -> Unit)
    fun getFileSize(uri: Uri): Long
    fun getFilename(uri: Uri): String
    fun getMimeType(uri: Uri): String
    fun getFileDestination(filename: String): Uri
}