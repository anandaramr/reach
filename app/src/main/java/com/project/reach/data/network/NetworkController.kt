package com.project.reach.data.network

import com.project.reach.domain.contracts.INetworkController
import com.project.reach.domain.contracts.IWifiController
import com.project.reach.network.model.Packet
import java.util.UUID

class NetworkController(
    private val wifiController: IWifiController
): INetworkController {
    override val packets = wifiController.packets
    override val newDevices = wifiController.newDevices

    override suspend fun sendPacket(
        userId: UUID,
        packet: Packet,
    ): Boolean {
        return when (packet) {
            is Packet.Message -> {
                sendPacketViaStream(userId, packet)
            }
            is Packet.Typing -> {
                sendPacketViaDatagram(userId, packet)
            }
            is Packet.Hello, is Packet.Heartbeat, is Packet.GoodBye -> {
                throw IllegalArgumentException("Discovery packets are not exposed by ${this::class.simpleName}")
            }
        }
    }

    private suspend fun sendPacketViaStream(userId: UUID, packet: Packet): Boolean {
        return wifiController.sendStream(userId, packet)
    }

    private suspend fun sendPacketViaDatagram(userId: UUID, packet: Packet): Boolean {
        return wifiController.sendDatagram(userId, packet)
    }
}