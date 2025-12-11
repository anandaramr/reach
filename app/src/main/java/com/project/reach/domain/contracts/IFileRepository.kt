package com.project.reach.domain.contracts

import android.net.Uri
import com.project.reach.data.utils.PrivateFile
import com.project.reach.domain.models.MessageState
import com.project.reach.domain.models.TransferState
import kotlinx.coroutines.flow.Flow
import java.io.InputStream
import java.io.OutputStream

interface IFileRepository {
    suspend fun useFileInputStream(
        uri: String,
        offset: Long,
        callback: suspend (InputStream) -> Unit
    )
    suspend fun useFileOutputStream(
        fileHash: String,
        offset: Long,
        callback: suspend (OutputStream) -> Unit
    )
    fun getDownloadLocation(fileHash: String): String
    suspend fun saveFileToPrivateStorage(uri: Uri, onProgress: (progress: Long) -> Unit): PrivateFile
    fun getContentUri(relativePath: String): Uri
    fun getFileSize(relativePath: String): Long
    fun isTransferOngoing(fileHash: String): Boolean
    suspend fun updateFileTransferProgress(fileHash: String, bytesRead: Long)
    suspend fun markAsNotInProgress(fileHash: String)
    fun observeTransferState(fileHash: String, fileSize: Long, messageState: MessageState): Flow<TransferState>
}