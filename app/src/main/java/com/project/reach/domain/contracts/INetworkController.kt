package com.project.reach.domain.contracts

import com.project.reach.network.model.DeviceInfo
import com.project.reach.network.model.Packet
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
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

    fun startDiscovery()
    fun stopDiscovery()
    fun release()
}