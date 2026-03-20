package com.project.reach.domain.contracts

import com.project.reach.domain.models.CallState
import com.project.reach.network.model.Packet
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

interface ICallRepository {
    val callState: StateFlow<CallState>

    suspend fun startCall(peerId: UUID)
    suspend fun acceptCall()
    suspend fun rejectCall()
    fun endCall()

    suspend fun onIceCandidateReceived(callId: UUID, candidate: Packet.CallSignal.IceCandidate)
    suspend fun onCallReceive(
        callId: UUID,
        peerId: UUID,
        peerUsername: String,
        sdpOffer: String
    )
    fun onPeerDecline(callId: UUID)
    fun onPeerDisconnect(callId: UUID)
    fun onPeerCancel(callId: UUID)
    suspend fun onPeerAccept(callId: UUID, peerId: UUID, sdpAnswer: String)
}