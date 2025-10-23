package com.project.reach.domain.models

sealed interface NotificationEvent {
    data class Message(
        val userId: String,
        val username: String,
        val message: String,
        val timeStamp: Long
    ): NotificationEvent
}