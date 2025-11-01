package com.project.reach.domain.models

data class Message(
    val text: String ,
    val isFromSelf: Boolean,
    val userId: String,
    val messageState: MessageState,
    val timeStamp: Long
)