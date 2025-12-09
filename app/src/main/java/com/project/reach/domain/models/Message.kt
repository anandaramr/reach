package com.project.reach.domain.models

import android.net.Uri

sealed interface Message {
    val messageId: String
    val isFromSelf: Boolean
    val messageState: MessageState
    val text: String
    val timeStamp: Long

    data class TextMessage(
        override val messageId: String,
        override val text: String,
        override val isFromSelf: Boolean,
        override val messageState: MessageState,
        override val timeStamp: Long
    ): Message

    data class FileMessage(
        override val messageId: String,
        override val text: String,
        override val isFromSelf: Boolean,
        override val timeStamp: Long,
        override val messageState: MessageState,

        val fileHash: String,
        val filename: String,
        val size: Long,
        val transferState: TransferState
    ): Message
}

sealed interface TransferState {
    object NotFound: TransferState
    object InProgress: TransferState
    data class Paused(val currentBytes: Long): TransferState
    data class Complete(val contentUri: Uri, val mimeType: String): TransferState
}