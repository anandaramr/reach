package com.project.reach.network.discovery

import com.project.reach.network.contracts.DiscoveryHandler
import com.project.reach.network.model.DeviceInfo
import com.project.reach.network.model.Packet
import com.project.reach.network.model.PacketWithSource
import com.project.reach.util.debug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.net.InetAddress

class UdpDiscoveryHandler(
    private val userId: String,
    private val username: String,
    private val packets: SharedFlow<PacketWithSource>,
    private val sendPacket: (ip: InetAddress, packet: Packet) -> Unit,
    private val onFound: (uuid: String, username: String) -> Boolean,
    private val onLost: (uuid: String) -> Unit
): DiscoveryHandler {
    private val _foundDevice = MutableSharedFlow<DeviceInfo>(extraBufferCapacity = 64, replay = 0)
    override val foundDevice = _foundDevice.asSharedFlow()

    private val _lostDevice = MutableSharedFlow<DeviceInfo>(extraBufferCapacity = 64, replay = 0)
    override val lostDevice = _lostDevice.asSharedFlow()

    private val deviceMap: MutableMap<String, DeviceActivityInfo> = mutableMapOf()

    private var isRunning = false

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var listenJob: Job? = null
    private var advertiseJob: Job? = null
    private var timeoutJob: Job? = null

    override fun start() {
        if (isRunning) return
        isRunning = true

        listenJob = scope.launch { listenToIncomingPackets() }
        advertiseJob = scope.launch { advertise() }
        timeoutJob = scope.launch { handleTimeout() }
    }

    override fun stop() {
        if (!isRunning) return
        isRunning = false

        sendPacket(InetAddress.getByName(BROADCAST_ADDR), Packet.GoodBye(userId))
        listenJob?.cancel()
        advertiseJob?.cancel()
        timeoutJob?.cancel()
    }

    override suspend fun resolvePeerAddress(uuid: String): InetAddress {
        val deviceInfo = deviceMap[uuid]
        if (deviceInfo == null) {
            throw NoSuchElementException("Cannot resolve IP address: user $uuid is not discoverable")
        }
        return deviceInfo.address
    }

    private suspend fun listenToIncomingPackets() {
        packets.collect { incoming ->
            val packet = incoming.packet
            when (packet) {
                is Packet.Hello -> {
                    handleDeviceFound(incoming.sourceIp, packet.userId, packet.username)
                    sendPacket(incoming.sourceIp, Packet.Heartbeat(userId, username))
                }

                is Packet.Heartbeat -> {
                    handleDeviceFound(incoming.sourceIp, packet.userId, packet.username)
                }

                is Packet.GoodBye -> {
                    handleDeviceLost(packet.userId)
                }

                is Packet.Message, is Packet.Typing -> {}
            }
        }
    }

    private fun handleDeviceFound(ip: InetAddress, userId: String, username: String) {
        if (userId == this.userId) return

        val deviceInfo = DeviceActivityInfo(address = ip, lastSeen = getCurrentTime())
        if (deviceMap.put(userId, deviceInfo) == null) {
            // announce new device discovery
            val valid = onFound(userId, username)

            // remove illegitimate device from device map
            if (!valid) deviceMap.remove(userId)
        }
    }

    private fun handleDeviceLost(userId: String) {
        if (userId == this.userId) return
        deviceMap.remove(userId)
        onLost(userId)
    }

    private suspend fun advertise() {
        val broadcastAddr = InetAddress.getByName(BROADCAST_ADDR)
        sendPacket(broadcastAddr, Packet.Hello(userId, username))

        while (isRunning) {
            delay(HEARTBEAT_INTERVAL)
            sendPacket(broadcastAddr, Packet.Heartbeat(userId, username))
        }
    }

    private suspend fun handleTimeout() {
        while (isRunning) {
            delay(TIMEOUT_INTERVAL)
            checkTimeout()
        }
    }

    private fun checkTimeout() {
        // need to use toList() first to avoid triggering
        deviceMap.forEach { (userId, deviceInfo) ->
            if (getCurrentTime() - deviceInfo.lastSeen > TIMEOUT_INTERVAL) {
                handleDeviceLost(userId)
            }
        }
    }

    override fun close() {
        stop()

        listenJob = null
        advertiseJob = null
        timeoutJob = null
    }

    override fun clear() {
        deviceMap.clear()
    }

    private fun getCurrentTime() = System.currentTimeMillis()

    private data class DeviceActivityInfo(
        val address: InetAddress,
        val lastSeen: Long
    )

    companion object {
        private const val BROADCAST_ADDR = "255.255.255.255"
        private const val HEARTBEAT_INTERVAL = 10 * 1000L
        private const val TIMEOUT_INTERVAL = HEARTBEAT_INTERVAL + 5000L
    }
}