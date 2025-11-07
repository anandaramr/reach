package com.project.reach.domain.models

data class Message(
    val messageId: String,
    val text: String,
    val messageType: MessageType,
    val metadata: String?,
    val isFromSelf: Boolean,
    val userId: String,
    val messageState: MessageState,
    val timeStamp: Long
)