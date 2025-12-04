package com.project.reach.domain.models

data class Message(
    val messageId: String,
    val messageType: MessageType,
    val text: String,
    val media: Media? = null,
    val isFromSelf: Boolean,
    val userId: String,
    val messageState: MessageState,
    val timeStamp: Long
)