package com.project.reach.domain.models

import java.util.UUID

sealed interface CallState {
    object Idle: CallState
    data class Incoming(val callId: UUID, val username: String): CallState
    data class Outgoing(val callId: UUID, val username: String): CallState
    data class Connected(val callId: UUID, val username: String): CallState
    data class Disconnected(val callId: UUID, val reason: String): CallState
}