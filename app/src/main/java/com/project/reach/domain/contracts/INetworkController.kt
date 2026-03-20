package com.project.reach.domain.contracts

import com.project.reach.network.model.DeviceInfo
import com.project.reach.network.model.Packet
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

interface INetworkController {
    val packets: SharedFlow<Packet>
    val newDevices: SharedFlow<DeviceInfo>
    val foundDevices: StateFlow<List<DeviceInfo>>
    suspend fun sendPacket(userId: UUID, packet: Packet): Boolean

    suspend fun acceptFile(
        peerId: UUID,
        fileId: String,
        outputStream: OutputStream,
        fileSize: Long,
        offset: Long,
        onProgress: (Long) -> Unit
    ): Boolean

    suspend fun sendFile(
        peerId: UUID,
        inputStream: InputStream,
        bytesToSend: Long,
        fileAccept: Packet.FileAccept,
        onProgress: (Long) -> Unit
    ): Boolean

    suspend fun initiateCall(peerId: UUID, callId: UUID, sdpOffer: SessionDescription): Boolean
    suspend fun sendIceCandidate(peerId: UUID, callId: UUID, candidate: IceCandidate): Boolean
    suspend fun acceptCall(peerId: UUID, callId: UUID, sdpAnswer: SessionDescription): Boolean
    suspend fun endCall(peerId: UUID, callId: UUID)

    fun startDiscovery()
    fun stopDiscovery()
    fun release()
    suspend fun declineCall(peerId: UUID, callId: UUID)
}