package com.project.reach.domain.contracts

import com.project.reach.network.model.DeviceInfo
import com.project.reach.network.model.Packet
import kotlinx.coroutines.flow.SharedFlow
import java.util.UUID

interface INetworkController {
    val packets: SharedFlow<Packet>
    val newDevices: SharedFlow<DeviceInfo>
    suspend fun sendPacket(userId: UUID, packet: Packet): Boolean
}