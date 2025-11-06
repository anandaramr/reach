package com.project.reach.network.discovery

import com.project.reach.network.contracts.DiscoveryHandler
import com.project.reach.network.model.Packet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.InetAddress

class HeartBeatDiscoveryHandler(
    private val myUserId: String,
    private val myUsername: StateFlow<String>,
    private val sendPacket: (ip: InetAddress, packet: Packet) -> Unit,
    private val onFound: (uuid: String, username: String) -> Boolean,
    private val onLost: (uuid: String) -> Unit
): DiscoveryHandler {
    private val deviceMap: MutableMap<String, DeviceActivityInfo> = mutableMapOf()

    private var isDiscovering = false

    private lateinit var supervisorJob: Job
    private lateinit var scope: CoroutineScope
    private var advertiseJob: Job? = null
    private var timeoutJob: Job? = null

    override fun start() {
        if (isDiscovering) return
        isDiscovering = true

        supervisorJob = SupervisorJob()
        scope = CoroutineScope(Dispatchers.IO + supervisorJob)

        advertiseJob = scope.launch { advertise() }
        timeoutJob = scope.launch { handleTimeout() }
    }

    override fun stop() {
        if (!isDiscovering) return
        isDiscovering = false

        sendPacket(InetAddress.getByName(BROADCAST_ADDR), Packet.GoodBye(myUserId))
        advertiseJob?.cancel()
        timeoutJob?.cancel()
        clear()
    }

    override suspend fun resolvePeerAddress(uuid: String): InetAddress {
        val deviceInfo = deviceMap[uuid]
        if (deviceInfo == null) {
            throw NoSuchElementException("Cannot resolve IP address: user $uuid is not discoverable")
        }
        return deviceInfo.address
    }

    fun handleIncomingPacket(ip: InetAddress, packet: Packet) {
        when (packet) {
            is Packet.Hello -> {
                handleDeviceFound(ip, packet.senderId, packet.senderUsername)
                sendPacket(ip, Packet.Heartbeat(myUserId, myUsername.value))
            }

            is Packet.Heartbeat -> {
                handleDeviceFound(ip, packet.senderId, packet.senderUsername)
            }

            is Packet.GoodBye -> {
                handleDeviceLost(packet.senderId)
            }

            is Packet.Message -> {
                // add device to discovered list if the user receives a message from it
                // temporarily handles cases where peer can discover user
                // but not vice versa, useful during bad network conditions
                handleDeviceFound(ip, packet.senderId, packet.senderUsername)
            }

            else -> {}
        }
    }

    private fun handleDeviceFound(ip: InetAddress, userId: String, username: String) {
        if (userId == this.myUserId) return

        val deviceInfo = DeviceActivityInfo(address = ip, lastSeen = getCurrentTime())
        if (deviceMap.put(userId, deviceInfo) == null) {
            // announce new device discovery
            val valid = onFound(userId, username)

            // remove illegitimate device from device map
            if (!valid) deviceMap.remove(userId)
        }
    }

    private fun handleDeviceLost(userId: String) {
        if (userId == this.myUserId) return
        deviceMap.remove(userId)
        onLost(userId)
    }

    private suspend fun advertise() {
        val broadcastAddr = InetAddress.getByName(BROADCAST_ADDR)
        sendPacket(broadcastAddr, Packet.Hello(myUserId, myUsername.value))

        while (isDiscovering) {
            delay(HEARTBEAT_INTERVAL)
            sendPacket(broadcastAddr, Packet.Heartbeat(myUserId, myUsername.value))
        }
    }

    private suspend fun handleTimeout() {
        while (isDiscovering) {
            delay(TIMEOUT_INTERVAL)
            checkTimeout()
        }
    }

    private fun checkTimeout() {
        // need to use toList() first to avoid triggering
        deviceMap.toList().forEach { (userId, deviceInfo) ->
            if (getCurrentTime() - deviceInfo.lastSeen > TIMEOUT_INTERVAL) {
                handleDeviceLost(userId)
            }
        }
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