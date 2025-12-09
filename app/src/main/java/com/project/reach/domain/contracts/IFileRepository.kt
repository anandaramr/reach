package com.project.reach.domain.contracts

import android.net.Uri
import com.project.reach.data.utils.IngestResult
import com.project.reach.domain.models.FileTransferState
import kotlinx.coroutines.flow.StateFlow
import java.io.InputStream
import java.io.OutputStream

interface IFileRepository {
    suspend fun useFileInputStream(uri: String, callback: suspend (InputStream) -> Unit)
    suspend fun useFileOutputStream(fileHash: String, callback: suspend (OutputStream) -> Unit)
    fun getDownloadLocation(filename: String): String
    suspend fun saveFileToPrivateStorage(uri: Uri): IngestResult
    fun getContentUri(relativePath: String): Uri
    fun updateFileTransferProgress(fileHash: String, progress: Long)
    fun markTransferAsComplete(fileHash: String)
//    fun getProgress(fileHash: String): StateFlow<FileTransferState>
}