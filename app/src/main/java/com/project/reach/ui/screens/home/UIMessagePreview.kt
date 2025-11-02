package com.project.reach.ui.screens.home

import com.project.reach.domain.models.MessageState
import kotlinx.coroutines.flow.StateFlow

data class UIMessagePreview(
    val userId: String,
    val username: String,
    val lastMessage: String,
    val messageState: MessageState,
    val timeStamp: Long,
    val isTyping: StateFlow<Boolean>
)
