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
import org.webrtc.RtpTransceiver
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
        val rtcConfig = PeerConnection.RTCConfiguration(emptyList())
        peerConnection = factory.createPeerConnection(rtcConfig, object: PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate?) {
                candidate?.let { onIceCandidateFound(it) }
            }

            override fun onTrack(transceiver: RtpTransceiver?) {
                val track = transceiver?.receiver?.track()
                if (track is AudioTrack) {
                    onTrackReceived(track)
                }
            }

            override fun onSignalingChange(s: PeerConnection.SignalingState?) {}
            override fun onIceConnectionChange(s: PeerConnection.IceConnectionState?) {}
            override fun onIceConnectionReceivingChange(b: Boolean) {}
            override fun onIceGatheringChange(s: PeerConnection.IceGatheringState?) {}
            override fun onIceCandidatesRemoved(a: Array<out IceCandidate>?) {}
            override fun onAddStream(stream: MediaStream?) {}
            override fun onRemoveStream(s: MediaStream?) {}
            override fun onDataChannel(d: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
        })

        localAudioSource = factory.createAudioSource(MediaConstraints())
        localAudioTrack = factory.createAudioTrack("ARDAMSa0", localAudioSource)
        peerConnection?.addTrack(localAudioTrack, listOf("ARDAMS"))
    }

    fun createOffer(callback: (SessionDescription) -> Unit) {
        peerConnection?.createOffer(object: SimpleSdpObserver() {
            override fun onCreateSuccess(sdp: SessionDescription) {
                peerConnection?.setLocalDescription(SimpleSdpObserver(), sdp)
                callback(sdp)
            }
        }, MediaConstraints())
    }

    fun onOfferReceived(remoteSdp: String, callback: (SessionDescription) -> Unit) {
        val remoteDesc = SessionDescription(SessionDescription.Type.OFFER, remoteSdp)
        peerConnection?.setRemoteDescription(object: SimpleSdpObserver() {
            override fun onSetSuccess() {
                createAnswer(callback)
            }

            override fun onSetFailure(error: String) {
                debug("WebRTC: Failed to set remote description after receiving offer: $error")
            }
        }, remoteDesc)
    }

    private fun createAnswer(callback: (SessionDescription) -> Unit) {
        peerConnection?.createAnswer(object: SimpleSdpObserver() {
            override fun onCreateSuccess(sdp: SessionDescription) {
                peerConnection?.setLocalDescription(SimpleSdpObserver(), sdp)
                callback(sdp)
            }
        }, MediaConstraints())
    }

    fun onAnswerReceived(remoteSdp: String) {
        val remoteDesc = SessionDescription(SessionDescription.Type.ANSWER, remoteSdp)
        peerConnection?.setRemoteDescription(SimpleSdpObserver(), remoteDesc)
    }

    fun addIceCandidate(candidate: IceCandidate) {
        peerConnection?.addIceCandidate(candidate)
    }

    fun disconnect() {
        peerConnection?.dispose()
        localAudioTrack?.dispose()
        localAudioSource?.dispose()
        peerConnection = null
        localAudioTrack = null
        localAudioSource = null
    }
}