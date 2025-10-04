package com.project.reach.ui.utils

sealed interface UIEvent {
    data class Error(val message: String): UIEvent
}