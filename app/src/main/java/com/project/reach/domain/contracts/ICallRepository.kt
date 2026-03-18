package com.project.reach.domain.contracts

import com.project.reach.domain.models.CallState
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

interface ICallRepository {
    val callState: StateFlow<CallState>

    fun startCall(peerId: UUID)
    fun onCallReceive(callId: UUID, peerId: UUID)
    fun onPeerAccept(callId: UUID)
    fun onPeerDecline(callId: UUID)
    fun onPeerDisconnect(callId: UUID)
    fun onPeerCancel(callId: UUID)

    fun acceptCall()
    fun rejectCall()
    fun endCall()
}