package com.project.reach.domain.contracts

import com.project.reach.domain.models.NetworkState
import com.project.reach.network.model.DeviceInfo
import com.project.reach.network.model.Packet
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

interface INetworkController {
    val networkState: StateFlow<NetworkState>
    val packets: SharedFlow<Packet>
    val newDevices: SharedFlow<DeviceInfo>
    val foundDevices: StateFlow<List<DeviceInfo>>
    suspend fun sendPacket(userId: UUID, packet: Packet): Boolean

    suspend fun sendFileHeader(
        peerId: UUID,
        fileId: UUID,
        filename: String,
        mimeType: String,
        size: Long
    ): Boolean

    suspend fun acceptFile(
        peerId: UUID,
        fileId: String,
        outputStream: OutputStream,
        size: Long
    ): Boolean

    suspend fun sendFile(
        peerId: UUID,
        inputStream: InputStream,
        size: Long,
        fileAccept: Packet.FileAccept
    ): Boolean

    fun startDiscovery()
    fun stopDiscovery()
    fun release()
}