package com.project.reach.domain.contracts

import android.net.Uri
import com.project.reach.data.utils.PrivateFile
import com.project.reach.domain.models.MessageState
import com.project.reach.domain.models.TransferState
import kotlinx.coroutines.flow.Flow
import java.io.InputStream
import java.io.OutputStream

/**
 * Repository interface for managing file operations with support for streaming I/O,
 * progress tracking, and transfer state management.
 */
interface IFileRepository {
    /**
     * Provides access to a file's input stream with automatic resource management.
     *
     * @param fileHash Unique identifier of the file to read
     * @param offset Starting position in bytes (supports resumable reads)
     * @param callback Suspended lambda receiving the InputStream for read operations
     *
     * @throws java.io.FileNotFoundException if file does not exist
     */
    suspend fun useFileInputStream(
        fileHash: String,
        offset: Long,
        callback: suspend (InputStream) -> Unit
    )

    /**
     * Provides safe access to a file's output stream with automatic resource management.
     *
     * @param fileHash Unique identifier of the file to write
     * @param offset Starting position in bytes (supports resumable writes)
     * @param callback Suspended lambda receiving the OutputStream for write operations
     */
    suspend fun useFileOutputStream(
        fileHash: String,
        offset: Long,
        callback: suspend (OutputStream) -> Unit
    )

    /**
     * Retrieves the file system path where the file is stored.
     *
     * @param fileHash Unique identifier of the file
     * @return Full path to the file location
     */
    fun getDownloadLocation(fileHash: String): String

    /**
     * Copies a file from an external URI to app's private storage.
     *
     * The [PrivateFile] object should be created by saving file to private
     * storage before sending the file using [IMessageRepository.sendMessage].
     * This method returns the SHA-256 hash of the file with the [PrivateFile]
     * object. Hashing and saving the file to private storage may take time, especially
     * for larger files, so the progress should be shown to the user by the way of the
     * [onProgress] callback
     *
     * @param uri Content provider URI of the from `content://`
     * @param onProgress Callback invoked with cumulative bytes copied
     * @return [PrivateFile] object containing file metadata and hash
     */
    suspend fun saveFileToPrivateStorage(uri: Uri, onProgress: (progress: Long) -> Unit): PrivateFile

    /**
     * Converts a relative path to a content URI for file access.
     *
     * Messages queried from database are returned as [com.project.reach.domain.models.Message]
     * which contain only the relative path. To open the file in private storage
     * with another app, access to the file should be granted by creating a content provider
     * URI
     *
     * @param relativePath Path relative to the storage root
     * @return Content URI for accessing the file
     */
    fun getContentUri(relativePath: String): Uri

    /**
     * Gets the total size of a file in bytes.
     *
     * @param fileHash Unique identifier of the file
     * @return File size in bytes
     */
    fun getFileSize(fileHash: String): Long

    /**
     * Checks if a file transfer (upload/download) is currently in progress.
     *
     * @param fileHash Unique identifier of the file
     * @return true if transfer is ongoing, false otherwise
     */
    fun isTransferOngoing(fileHash: String): Boolean

    /**
     * Updates the transfer progress for a file operation.
     *
     * @param fileHash Unique identifier of the file being transferred
     * @param bytesRead Number of bytes transferred so far
     */
    suspend fun updateFileTransferProgress(fileHash: String, bytesRead: Long)

    /**
     * Marks a file transfer as complete or paused.
     *
     * @param fileHash Unique identifier of the file
     */
    suspend fun markAsNotInProgress(fileHash: String)

    /**
     * Creates a Flow that emits transfer state updates for a file.
     *
     * @param fileHash Unique identifier of the file
     * @param messageState Current state of the associated message
     * @return Flow emitting TransferState updates as the transfer progresses
     */
    fun observeTransferState(fileHash: String, messageState: MessageState): Flow<TransferState>
}