package com.project.reach.data.network

import com.project.reach.data.local.IdentityManager
import com.project.reach.domain.contracts.INetworkController
import com.project.reach.domain.contracts.IWifiController
import com.project.reach.domain.models.NetworkState
import com.project.reach.network.model.Packet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class NetworkController(
    private val wifiController: IWifiController,
    identityManager: IdentityManager
): INetworkController {
    private val supervisorJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + supervisorJob)

    private val myUserId = identityManager.userId

    override val networkState =
        wifiController.isActive.map { if (it) NetworkState.WIFI else NetworkState.NONE }.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NetworkState.NONE
        )

    override val packets = wifiController.packets
    override val newDevices = wifiController.newDevices
    override val foundDevices = wifiController.foundDevices

    override suspend fun sendPacket(
        userId: UUID,
        packet: Packet,
    ): Boolean {
        return when (packet) {
            is Packet.Message, is Packet.FileAccept -> {
                sendPacketViaStream(userId, packet)
            }

            is Packet.Typing -> {
                sendPacketViaDatagram(userId, packet)
            }

            is Packet.Hello, is Packet.Heartbeat, is Packet.GoodBye -> {
                throw IllegalArgumentException("Discovery packets sends are not exposed by ${this::class.simpleName}")
            }
        }
    }

    override suspend fun acceptFile(
        peerId: UUID,
        fileId: String,
        outputStream: OutputStream,
        size: Long,
        onProgress: (Long) -> Unit
    ): Boolean {
        val dataInputChannel = wifiController.getDataInputChannel(peerId)
        if (dataInputChannel == null) return false

        dataInputChannel.use { channel ->
            val sendResult = sendPacketViaStream(
                userId = peerId, Packet.FileAccept(
                    senderId = myUserId,
                    fileHash = fileId,
                    port = channel.port
                )
            )

            if (!sendResult) return false
            return channel.readInto(outputStream, size, onProgress)
        }
    }

    override suspend fun sendFile(
        peerId: UUID,
        inputStream: InputStream,
        size: Long,
        fileAccept: Packet.FileAccept,
        onProgress: (Long) -> Unit
    ): Boolean {
        val port = fileAccept.port
        val dataOutputChannel = wifiController.getDataOutputChannel(peerId, port)
        if (dataOutputChannel == null) return false

        dataOutputChannel.use { channel ->
            return channel.writeFrom(inputStream, size = size, onProgress)
        }
    }

    override fun startDiscovery() {
        wifiController.startDiscovery()
    }

    override fun stopDiscovery() {
        wifiController.stopDiscovery()
    }

    override fun release() {
        wifiController.close()
    }

    private suspend fun sendPacketViaStream(userId: UUID, packet: Packet): Boolean {
        return wifiController.sendStream(userId, packet)
    }

    private suspend fun sendPacketViaDatagram(userId: UUID, packet: Packet): Boolean {
        return wifiController.sendDatagram(userId, packet)
    }
}