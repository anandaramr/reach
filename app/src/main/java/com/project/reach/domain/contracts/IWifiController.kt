package com.project.reach.domain.contracts

import com.project.reach.network.model.Packet
import com.project.reach.network.model.DeviceInfo
import com.project.reach.network.transport.DataInputChannel
import com.project.reach.network.transport.DataOutputChannel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

interface IWifiController {
    val foundDevices: StateFlow<List<DeviceInfo>>
    val newDevices: SharedFlow<DeviceInfo>
    val packets: SharedFlow<Packet>

    fun start()
    suspend fun sendDatagram(uuid: UUID, packet: Packet): Boolean
    suspend fun sendStream(uuid: UUID, packet: Packet): Boolean
    suspend fun getDataInputChannel(uuid: UUID): DataInputChannel?
    suspend fun getDataOutputChannel(uuid: UUID, port: Int): DataOutputChannel?
    fun stop()
}