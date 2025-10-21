package com.project.reach.domain.models

import java.util.UUID

data class MessagePreview(
    val userId: UUID,
    val username: String,
    val text: String,
    val messageState: MessageState,
    val timeStamp: Long
)
