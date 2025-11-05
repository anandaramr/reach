package com.project.reach.data.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class TypingStateHandler(
    private val scope: CoroutineScope
) {
    private val typingJobs = ConcurrentHashMap<String, Job>()
    private val _typingUsers = MutableStateFlow<Set<String>>(emptySet())
    val typingUsers = _typingUsers.asStateFlow()
    private var lastSentTime: Long = 0

    fun setIsTyping(userId: String) {
        _typingUsers.update { it + userId }

        // operations handled atomically to prevent race conditions
        typingJobs.compute(userId) { _, job ->
            job?.cancel()
            scope.launch {
                delay(TYPING_CHECK_DELAY)
                _typingUsers.update { it - userId }
                typingJobs.remove(userId)
            }
        }
    }

    fun resetPeerIsTyping(userId: String) {
        _typingUsers.update { it - userId }
        typingJobs.remove(userId)
    }

    fun resetSelfIsTyping() {
        lastSentTime = 0
    }

    fun getIsTyping(userId: String): Flow<Boolean> {
        return _typingUsers.map { it.contains(userId) }
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