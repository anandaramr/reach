package com.project.reach.data.respository

import com.project.reach.domain.contracts.ICallRepository
import com.project.reach.domain.models.CallState
import com.project.reach.util.debug
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class CallRepository: ICallRepository {
    private val _callState = MutableStateFlow<CallState>(CallState.Idle)
    override val callState: StateFlow<CallState> = _callState.asStateFlow()

    override fun startCall(peerId: UUID) {
        if (_callState.value == CallState.Idle) {
            // update state and make call
        } else {
            debug("Call: cannot make multiple simultaneous calls")
        }
    }

    override fun onCallReceive(callId: UUID, peerId: UUID) {
        if (_callState.value == CallState.Idle) {
            // update state and handle incoming call
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
            // handle decline
        } else {
            debug("Decline received for invalid call")
        }
    }

    override fun onPeerDisconnect(callId: UUID) {
        val state = _callState.value
        if (state is CallState.Connected && state.callId == callId) {
            // handle disconnect
        } else {
            debug("Disconnect received for invalid call")
        }
    }

    override fun onPeerCancel(callId: UUID) {
        val state = _callState.value
        if (state is CallState.Incoming && state.callId == callId || state is CallState.Connected && state.callId == callId) {
            // handle cancel
        } else {
            debug("Cancel received for invalid call")
        }
    }

    override fun acceptCall() {
        if (_callState.value is CallState.Incoming) {
            // handle accept
        } else {
            debug("Invalid call accept")
        }
    }

    override fun rejectCall() {
        if (_callState.value is CallState.Incoming) {
            // handle reject
        } else {
            debug("Invalid call reject")
        }
    }

    override fun endCall() {
        if (_callState.value is CallState.Connected) {
            // handle disconnect
        } else {
            debug("Invalid end call")
        }
    }
}