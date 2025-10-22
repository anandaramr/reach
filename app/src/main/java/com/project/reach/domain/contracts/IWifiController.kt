package com.project.reach.domain.contracts

import com.project.reach.network.model.Packet
import com.project.reach.network.model.DeviceInfo
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

interface IWifiController {
    val isActive: StateFlow<Boolean>
    val foundDevices: StateFlow<List<DeviceInfo>>
    val packets: SharedFlow<Packet>

    fun startDiscovery()
    fun send(uuid: UUID, packet: Packet): Boolean
    fun stopDiscovery()

    fun close()
}