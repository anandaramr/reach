package com.project.reach.domain.models

sealed interface NotificationEvent {
    data class Message(val username: String, val message: String)
}