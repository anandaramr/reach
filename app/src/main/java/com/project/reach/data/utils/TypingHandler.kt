package com.project.reach.data.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TypingStateHandler(
    private val scope: CoroutineScope
) {
    private val typingJobs = mutableMapOf<String, Job>()
    private val typingUsers = MutableStateFlow<Set<String>>(emptySet())
    private var lastSentTime: Long = 0

    fun setIsTyping(userId: String) {
        typingJobs[userId]?.cancel()
        typingUsers.update { it + userId }

        val job = scope.launch {
            delay(timeMillis = TYPING_CHECK_DELAY)
            typingUsers.update { it - userId }
            typingJobs.remove(userId)
        }
        typingJobs.put(userId, job)
    }

    fun getIsTyping(userId: String): Flow<Boolean> {
        return typingUsers
            .map {
                it.contains(userId)
            }
    }

    fun throttledSend(send: () -> Unit) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSentTime > THROTTLE_INTERVAL) {
            send()
            lastSentTime = currentTime
        }
    }

    companion object {
        private const val TYPING_CHECK_DELAY = 3000L
        private const val THROTTLE_INTERVAL = 1000L
    }
}