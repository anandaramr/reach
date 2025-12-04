package com.project.reach.domain.contracts

import java.io.InputStream
import java.io.OutputStream

interface IFileRepository {
    suspend fun useFileInputStream(filename: String, callback: suspend (inputStream: InputStream) -> Unit)
    suspend fun useFileOutputStream(filename: String, callback: suspend (outputStream: OutputStream) -> Unit)
    fun getFileSize(filename: String): Long
}