package com.project.reach.data.network

import android.content.Context
import com.project.reach.util.debug
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.RtpTransceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.audio.JavaAudioDeviceModule

class WebRtcSessionManager(
    private val context: Context,
    private val onIceCandidateFound: (IceCandidate) -> Unit,
    private val onTrackReceived: (AudioTrack) -> Unit
) {
    private var peerConnection: PeerConnection? = null
    private val factory: PeerConnectionFactory by lazy { createFactory() }

    private fun createFactory(): PeerConnectionFactory {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)

        return PeerConnectionFactory.builder()
            .setAudioDeviceModule(JavaAudioDeviceModule.builder(context).createAudioDeviceModule())
            .createPeerConnectionFactory()
    }

    private var localAudioTrack: AudioTrack? = null
    private var localAudioSource: AudioSource? = null
    fun initPeerConnection() {
        if (peerConnection != null) {
            debug("[WebRTC] attempt to call duplicate initPeerConnection()")
            return
        }
        val rtcConfig = PeerConnection.RTCConfiguration(emptyList()).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            iceTransportsType = PeerConnection.IceTransportsType.ALL
        }

        peerConnection = factory.createPeerConnection(rtcConfig, object: PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate) {
                onIceCandidateFound(candidate)
            }

            override fun onTrack(transceiver: RtpTransceiver) {
                val track = transceiver.receiver.track()
                if (track is AudioTrack) onTrackReceived(track)
            }

            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {
                debug("IceConnectionState: $state")
            }
            override fun onSignalingChange(state: PeerConnection.SignalingState) {}
            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState) {}
            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>) {}
            override fun onAddStream(stream: MediaStream) {}
            override fun onRemoveStream(stream: MediaStream) {}
            override fun onDataChannel(channel: DataChannel) {}
            override fun onRenegotiationNeeded() {}
            override fun onIceConnectionReceivingChange(receiving: Boolean) {}
            override fun onAddTrack(receiver: RtpReceiver, streams: Array<out MediaStream>) {}
        })

        // Add local audio track
        val audioConstraints = MediaConstraints()
        localAudioSource = factory.createAudioSource(audioConstraints)
        localAudioTrack = factory.createAudioTrack("audio0", localAudioSource)
        localAudioTrack?.setEnabled(true)
        peerConnection?.addTrack(localAudioTrack, listOf("stream0"))
    }

    fun createOffer(callback: (SessionDescription) -> Unit) {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        }
        peerConnection?.createOffer(object: SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription) {
                peerConnection?.setLocalDescription(object: SdpObserver {
                    override fun onSetSuccess() = callback(sdp)
                    override fun onSetFailure(error: String) {}
                    override fun onCreateSuccess(sdp: SessionDescription) {}
                    override fun onCreateFailure(error: String) {}
                }, sdp)
            }

            override fun onCreateFailure(error: String) {}
            override fun onSetSuccess() {}
            override fun onSetFailure(error: String) {}
        }, constraints)
    }

    fun onOfferReceived(remoteSdp: String) {
        val sdp = SessionDescription(SessionDescription.Type.OFFER, remoteSdp)
        peerConnection?.setRemoteDescription(object: SdpObserver {
            override fun onSetSuccess() {
                debug("[WebRTC] Set remote offer description")
                pendingCandidates.forEach { peerConnection?.addIceCandidate(it) }
                pendingCandidates.clear()
            }

            override fun onSetFailure(error: String) {
                debug("[WebRTC] Failed to set remote offer: $error")
            }
            override fun onCreateSuccess(sdp: SessionDescription) {}
            override fun onCreateFailure(error: String) {}
        }, sdp)
    }

    fun createAnswer(callback: (SessionDescription) -> Unit) {
        val constraints = MediaConstraints()
        peerConnection?.createAnswer(object: SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription) {
                peerConnection?.setLocalDescription(object: SdpObserver {
                    override fun onSetSuccess() = callback(sdp)
                    override fun onSetFailure(error: String) {}
                    override fun onCreateSuccess(sdp: SessionDescription) {}
                    override fun onCreateFailure(error: String) {}
                }, sdp)
            }

            override fun onCreateFailure(error: String) {}
            override fun onSetSuccess() {}
            override fun onSetFailure(error: String) {}
        }, constraints)
    }

    fun onAnswerReceived(remoteSdp: String) {
        val sdp = SessionDescription(SessionDescription.Type.ANSWER, remoteSdp)
        peerConnection?.setRemoteDescription(object: SdpObserver {
            override fun onSetSuccess() {
                debug("[WebRTC] Set remote answer description")
                pendingCandidates.forEach { peerConnection?.addIceCandidate(it) }
                pendingCandidates.clear()
            }

            override fun onSetFailure(error: String) {
                debug("[WebRTC] Failed to set remote answer. Reason: $error")
            }
            override fun onCreateSuccess(sdp: SessionDescription) {}
            override fun onCreateFailure(error: String) {}
        }, sdp)
    }

    private val pendingCandidates = mutableListOf<IceCandidate>()

    fun addIceCandidate(candidate: IceCandidate) {
        if (peerConnection?.remoteDescription == null) {
            pendingCandidates.add(candidate)
        } else {
            peerConnection?.addIceCandidate(candidate)
        }
    }

    fun disconnect() {
        peerConnection?.close()
        peerConnection?.dispose()
        localAudioTrack?.dispose()
        localAudioSource?.dispose()
        peerConnection = null
        localAudioTrack = null
        localAudioSource = null
    }
}