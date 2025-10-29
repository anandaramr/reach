package com.project.reach.domain.models

sealed interface NotificationEvent {
    data class Message(
        val userId: String,
        val username: String,
        val messages: List<MessageNotification>,
    ): NotificationEvent
}

data class MessageNotification(
    val text: String,
    val timeStamp: Long
)