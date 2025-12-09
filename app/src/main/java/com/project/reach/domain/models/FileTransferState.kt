package com.project.reach.domain.models

sealed interface FileTransferState {
    object Preparing: FileTransferState
    object FileNotFound: FileTransferState
    object Incomplete: FileTransferState

    class Progress(
        val receivedBytes: Long,
        val totalBytes: Long
    ): FileTransferState

    object Completed: FileTransferState
}