package com.project.reach.ui.screens.chat.components

import androidx.compose.runtime.Composable
import com.project.reach.domain.models.Message

@Composable
fun MessageDisplay(message: Message.TextMessage) {
    ChatBubble(message = message)
}