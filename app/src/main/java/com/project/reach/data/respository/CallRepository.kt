package com.project.reach.data.respository

import com.project.reach.data.local.IdentityManager
import com.project.reach.domain.contracts.ICallRepository
import com.project.reach.domain.contracts.IContactRepository
import com.project.reach.domain.contracts.INetworkController
import com.project.reach.domain.models.CallState
import com.project.reach.network.model.Packet
import com.project.reach.util.debug
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.UUID

class CallRepository(
    private val contactRepository: IContactRepository,
    private val networkController: INetworkController,
    identityManager: IdentityManager
): ICallRepository {
    private val _callState = MutableStateFlow<CallState>(CallState.Idle)
    override val callState: StateFlow<CallState> = _callState.asStateFlow()

    private val mUserId = identityManager.userId
    private val mUsername = identityManager.username

    override suspend fun startCall(peerId: UUID) = withContext(Dispatchers.IO) {
        val userExists = contactRepository.userEntryExists(peerId)
        if (!userExists) {
            throw IllegalArgumentException("startCall: user entry not found")
        }

        if (_callState.value == CallState.Idle) {
            val contact = contactRepository.getContact(peerId.toString()).first()
            val callId = UUID.randomUUID()
            val result = networkController.sendPacket(
                userId = peerId,
                Packet.CallSignal.CallInit(
                    callId = callId.toString(),
                    senderId = mUserId,
                    senderUsername = mUsername.value
                )
            )

            if (!result) return@withContext
            _callState.value = CallState.Outgoing(
                callId = callId,
                peerId = peerId,
                username = contact.username,
                nickname = contact.nickname
            )
        } else {
            debug("Call: cannot make multiple simultaneous calls")
        }
    }

    override fun acceptCall() {
        val state = _callState.value
        if (state is CallState.Incoming) {
            _callState.value = CallState.Connected(
                callId = state.callId,
                peerId = state.peerId,
                username = state.username,
                nickname = state.nickname
            )
        } else {
            debug("Invalid call accept [state: ${_callState.value}]")
        }
    }

    override suspend fun rejectCall() = withContext(Dispatchers.IO) {
        val state = _callState.value
        if (state is CallState.Incoming) {
            // handle reject
            networkController.sendPacket(
                userId = state.peerId,
                packet = Packet.CallSignal.CallDecline(
                    callId = state.callId.toString(),
                    senderId = mUserId
                ),
            )
            _callState.value = CallState.Idle
        } else {
            debug("Invalid call reject [state: $state]")
        }
    }

    override fun endCall() {
        if (_callState.value is CallState.Connected ||
            _callState.value is CallState.Outgoing ||
            _callState.value is CallState.Incoming
        ) {
            // handle disconnect
            _callState.value = CallState.Idle
        } else {
            debug("Invalid end call [state: ${_callState.value}]")
        }
    }

    override suspend fun onCallReceive(callId: UUID, peerId: UUID, peerUsername: String) {
        contactRepository.addToContactsIfNotExists(peerId.toString(), peerUsername)

        if (_callState.value == CallState.Idle) {
            val contact = contactRepository.getContact(peerId.toString()).first()
            _callState.value = CallState.Incoming(callId, peerId, peerUsername, contact.nickname)
        } else {
            // handle busy rejections
        }
    }

    override fun onPeerAccept(callId: UUID) {
        val state = _callState.value
        if (state is CallState.Outgoing && state.callId == callId) {
            // handle accept
        } else {
            debug("Accept received for invalid call")
        }
    }

    override fun onPeerDecline(callId: UUID) {
        val state = _callState.value
        if (state is CallState.Outgoing && state.callId == callId) {
            _callState.value = CallState.Idle
        } else {
            debug("Decline received for invalid call")
        }
    }

    override fun onPeerDisconnect(callId: UUID) {
        val state = _callState.value
        if (state is CallState.Connected && state.callId == callId) {
            _callState.value = CallState.Idle
        } else {
            debug("Disconnect received for invalid call")
        }
    }

    override fun onPeerCancel(callId: UUID) {
        val state = _callState.value
        if (state is CallState.Incoming && state.callId == callId || state is CallState.Connected && state.callId == callId) {
            _callState.value = CallState.Idle
        } else {
            debug("Cancel received for invalid call")
        }
    }

    override suspend fun onIceCandidateReceived(
        callId: UUID,
        candidate: Packet.CallSignal.IceCandidate
    ) {
        TODO("Not yet implemented")
    }
}