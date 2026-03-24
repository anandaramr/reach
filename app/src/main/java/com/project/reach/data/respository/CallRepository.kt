package com.project.reach.data.respository

import android.content.Context
import com.project.reach.data.network.WebRtcSessionManager
import com.project.reach.domain.contracts.ICallRepository
import com.project.reach.domain.contracts.IContactRepository
import com.project.reach.domain.contracts.INetworkController
import com.project.reach.domain.models.CallState
import com.project.reach.network.model.Packet
import com.project.reach.util.debug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.webrtc.AudioTrack
import org.webrtc.IceCandidate
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CallRepository(
    private val contactRepository: IContactRepository,
    private val networkController: INetworkController,
    private val context: Context
): ICallRepository {
    private val _callState = MutableStateFlow<CallState>(CallState.Idle)
    override val callState: StateFlow<CallState> = _callState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val webRtcSessionManager = WebRtcSessionManager(
        context = context,
        onIceCandidateFound = ::onCandidateFound,
        onTrackReceived = ::onTrackReceived,
    )

    override suspend fun startCall(peerId: UUID) = withContext(Dispatchers.IO) {
        val userExists = contactRepository.userEntryExists(peerId)
        if (!userExists) {
            throw IllegalArgumentException("startCall: user entry not found")
        }

        val contact = contactRepository.getContact(peerId.toString()).first()
        val callId = UUID.randomUUID()

        val prevState = _callState.getAndUpdate { state ->
            if (canStartCall(state)) CallState.Outgoing(
                callId = callId,
                peerId = peerId,
                username = contact.username,
                nickname = contact.nickname
            ) else {
                debug("Cannot start call while already in a call [$state]")
                state
            }
        }
        if (!canStartCall(prevState)) return@withContext

        webRtcSessionManager.initPeerConnection()
        val sdp = suspendCoroutine { continuation ->
            webRtcSessionManager.createOffer { sdp ->
                continuation.resume(sdp)
            }
        }
        val result = networkController.initiateCall(peerId, callId, sdp)
        if (!result) {
            webRtcSessionManager.disconnect()
            _callState.value = CallState.Disconnected(
                callId,
                reason = "Could not reach user ${contact.nickname ?: contact.username}",
                peerId = peerId,
                username = contact.username,
                nickname = contact.nickname,
            )
        }
    }

    override suspend fun acceptCall() = withContext(Dispatchers.IO) {
        val prevState = _callState.getAndUpdate { state ->
            if (state is CallState.Incoming) {
                CallState.Connected(
                    callId = state.callId,
                    peerId = state.peerId,
                    username = state.username,
                    nickname = state.nickname
                )
            } else {
                debug("Invalid call accept [state: ${_callState.value}]")
                state
            }
        }
        if (prevState !is CallState.Incoming) return@withContext

        val sdp = suspendCoroutine { continuation ->
            webRtcSessionManager.createAnswer { sdp ->
                continuation.resume(sdp)
            }
        }
        networkController.acceptCall(prevState.peerId, prevState.callId, sdp)
    }

    override suspend fun rejectCall() = withContext(Dispatchers.IO) {
        val prevState = _callState.getAndUpdate { state ->
            if (state is CallState.Incoming) {
                CallState.Idle
            } else {
                debug("Invalid call reject [state: $state]")
                state
            }
        }
        if (prevState !is CallState.Incoming) return@withContext
        scope.launch { networkController.declineCall(prevState.peerId, prevState.callId) }
        webRtcSessionManager.disconnect()
    }

    override fun endCall() {
        val prevState = _callState.getAndUpdate { state ->
            if (state is CallState.Connected || state is CallState.Outgoing) {
                CallState.Idle
            } else {
                debug("Invalid end call [state: ${_callState.value}]")
                state
            }
        }
        if (prevState is CallState.Connected) {
            scope.launch { networkController.endCall(prevState.peerId, prevState.callId) }
            webRtcSessionManager.disconnect()
        } else if (prevState is CallState.Outgoing) {
            scope.launch { networkController.endCall(prevState.peerId, prevState.callId) }
            webRtcSessionManager.disconnect()
        }
    }

    override fun resetCall() {
        _callState.update { state ->
            if (state != CallState.Idle) {
                CallState.Idle
            } else {
                debug("Call already in Idle state")
                state
            }
        }
    }

    override suspend fun onCallReceive(
        callId: UUID,
        peerId: UUID,
        peerUsername: String,
        sdpOffer: String
    ) {
        contactRepository.addToContactsIfNotExists(peerId.toString(), peerUsername)
        _callState.update { state ->
            if (state == CallState.Idle || state is CallState.Disconnected) {
                val contact = contactRepository.getContact(peerId.toString()).first()
                webRtcSessionManager.initPeerConnection()
                webRtcSessionManager.onOfferReceived(sdpOffer)
                CallState.Incoming(callId, peerId, peerUsername, contact.nickname)
            } else {
                // handle busy rejections
                state
            }
        }
    }

    override suspend fun onPeerAccept(callId: UUID, peerId: UUID, sdpAnswer: String) {
        _callState.update { state ->
            if (state is CallState.Outgoing && state.callId == callId) {
                val contact = contactRepository.getContact(peerId.toString()).first()
                webRtcSessionManager.onAnswerReceived(sdpAnswer)
                CallState.Connected(callId, peerId, contact.username, contact.nickname)
            } else {
                debug("Accept received for invalid call")
                state
            }
        }
    }

    override fun onPeerDecline(callId: UUID) {
        _callState.update { state ->
            if (state is CallState.Outgoing && state.callId == callId) {
                webRtcSessionManager.disconnect()
                CallState.Disconnected(
                    callId,
                    reason = "Call declined",
                    peerId = state.peerId,
                    username = state.username,
                    nickname = state.nickname
                )
            } else {
                debug("Decline received for invalid call")
                state
            }
        }
    }

    override fun onPeerDisconnect(callId: UUID) {
        _callState.update { state ->
            if (state is CallState.Connected && state.callId == callId ||
                state is CallState.Incoming && state.callId == callId
            ) {
                webRtcSessionManager.disconnect()
                CallState.Idle
            } else {
                debug("Disconnect received for invalid call")
                state
            }
        }
    }

    override suspend fun onIceCandidateReceived(
        callId: UUID,
        candidate: Packet.CallSignal.IceCandidate
    ) {
        val result = getPeerAndCallIdIfCallActive() ?: return
        val (_, currentCallId) = result
        if (callId != currentCallId) return
        webRtcSessionManager.addIceCandidate(
            candidate = IceCandidate(
                candidate.sdpMid,
                candidate.mLineIndex,
                candidate.candidate
            )
        )
    }

    private fun onTrackReceived(audioTrack: AudioTrack) {
        debug("Audio track received")
        audioTrack.setEnabled(true)
        audioTrack.setVolume(1.0)
    }

    private fun onCandidateFound(candidate: IceCandidate) {
        val result = getPeerAndCallIdIfCallActive() ?: return
        val (peerId, callId) = result
        scope.launch {
            networkController.sendIceCandidate(
                peerId = peerId,
                callId = callId,
                candidate = candidate
            )
        }
    }

    private fun canStartCall(currentState: CallState): Boolean =
        currentState == CallState.Idle || currentState is CallState.Disconnected

    private fun getPeerAndCallIdIfCallActive(): Pair<UUID, UUID>? {
        val state = _callState.value
        if (state is CallState.Incoming) return Pair(state.peerId, state.callId)
        if (state is CallState.Outgoing) return Pair(state.peerId, state.callId)
        if (state is CallState.Connected) return Pair(state.peerId, state.callId)
        return null
    }
}