package com.project.reach.domain.contracts

import android.net.Uri
import com.project.reach.data.utils.PrivateFile
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.ConcurrentHashMap

interface IFileRepository {
    val activeFileTransfers: ConcurrentHashMap<String, Long>
    suspend fun useFileInputStream(uri: String, callback: suspend (InputStream) -> Unit)
    suspend fun useFileOutputStream(fileHash: String, callback: suspend (OutputStream) -> Unit)
    fun getDownloadLocation(fileHash: String): String
    suspend fun saveFileToPrivateStorage(uri: Uri, onProgress: (progress: Long) -> Unit): PrivateFile
    fun getContentUri(relativePath: String): Uri?
    fun updateFileTransferProgress(fileHash: String, progress: Long)
    fun markAsNotInProgress(fileHash: String)
    fun getFileSize(fileHash: String): Long
}