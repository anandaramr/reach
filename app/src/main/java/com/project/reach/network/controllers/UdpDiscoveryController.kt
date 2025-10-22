package com.project.reach.network.controllers

import com.project.reach.data.local.IdentityManager
import com.project.reach.network.model.DeviceInfo
import com.project.reach.util.debug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketException
import java.util.UUID

// TODO: Make more robust
class UdpDiscoveryController(
    private val identityManager: IdentityManager
) {
    private val username = identityManager.getUsernameIdentity()
    private val uuid = UUID.fromString(identityManager.getUserUUID())

    private val _foundServices = MutableStateFlow<List<DeviceInfo>>(emptyList())
    val foundServices: StateFlow<List<DeviceInfo>> = _foundServices.asStateFlow()

    private val deviceMap: MutableMap<UUID, DeviceActivityInfo> = mutableMapOf()

    private val socket by lazy {
        DatagramSocket(DISCOVERY_PORT).apply {
            broadcast = true
            reuseAddress = true
        }
    }

    private var isRunning = false
    private var listenJob: Job? = null
    private var broadcastJob: Job? = null
    private var timeoutJob: Job? = null

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun startDiscovery() {
        if (isRunning) return
        isRunning = true

        broadcastJob = scope.launch { startBroadcasting() }
        listenJob = scope.launch { listenToBroadcast() }
        timeoutJob = scope.launch { checkTimeOut() }
    }

    private suspend fun startBroadcasting() {
        while (isRunning) {
            sendBroadcastPacket("HEARTBEAT:$uuid:$username")
            delay(HEARTBEAT_INTERVAL)
        }
    }

    private suspend fun checkTimeOut() {
        while (isRunning) {
            delay(TIMEOUT_INTERVAL)
            val timedOutDevices =
                _foundServices.value.filter { getCurrentTime() - deviceMap[it.uuid]!!.lastSeen > HEARTBEAT_INTERVAL }
            timedOutDevices.forEach { handleDeviceLost(it.uuid) }
        }
    }

    private suspend fun listenToBroadcast() {
        while (isRunning) {
            val buffer = ByteArray(1024)
            val packet = DatagramPacket(
                buffer,
                buffer.size,
                InetAddress.getByName(BROADCAST_ADDRESS),
                DISCOVERY_PORT
            )
            try {
                socket.receive(packet)
                handleReceived(
                    packet.data.copyOf(packet.length),
                    packet.address, packet.port
                )
            } catch (e: SocketException) {
                debug("error: $e")
            }
        }
    }

    private fun sendBroadcastPacket(message: String) {
        sendPacket(message, InetAddress.getByName(BROADCAST_ADDRESS), DISCOVERY_PORT)
    }

    private fun sendPacket(message: String, address: InetAddress, port: Int) {
        val buffer = message.toByteArray()
        val packet = DatagramPacket(
            buffer,
            buffer.size,
            InetAddress.getByName(BROADCAST_ADDRESS),
            DISCOVERY_PORT
        )
        try {
            socket.send(packet)
        } catch (e: SocketException) {
            debug("error: $e")
        }
    }

    private fun handleReceived(bytes: ByteArray, address: InetAddress, port: Int) {
        val message = bytes.decodeToString()
        val parts = message.split(':')
        if (parts.size != 3) return

        val type = parts[0]
        val uuid = UUID.fromString(parts[1])

        when (type) {
            "HEARTBEAT" -> {
                val username = parts[2]
                handleDeviceFound(uuid, username, address, port)
            }

            "GOODBYE" -> {
                handleDeviceLost(uuid)
            }
        }
    }

    private fun handleDeviceFound(uuid: UUID, username: String, address: InetAddress, port: Int) {
        if (uuid == this.uuid) return

        if (deviceMap.contains(uuid)) {
            val info = deviceMap[uuid]
            if (info != null) {
                deviceMap.put(uuid, info.copy(lastSeen = getCurrentTime()))
            }
        } else {
            deviceMap.put(uuid, DeviceActivityInfo(address, port, lastSeen = getCurrentTime()))
            _foundServices.value += DeviceInfo(uuid, username)
        }
    }

    private fun handleDeviceLost(uuid: UUID) {
        if (uuid == this.uuid) return

        deviceMap.remove(uuid)
        _foundServices.update {
            it.filter { service -> service.uuid != uuid }
        }
    }

    fun close() {
        scope.launch {
            sendBroadcastPacket("GOODBYE:$uuid:$username")
        }

        isRunning = false
        runCatching {
            listenJob?.cancel()
            broadcastJob?.cancel()
            timeoutJob?.cancel()
            socket.close()
        }
    }

    private fun getCurrentTime(): Long = System.currentTimeMillis()

    companion object {
        private const val DISCOVERY_PORT = 5000
        private const val BROADCAST_ADDRESS = "255.255.255.255"
        private const val HEARTBEAT_INTERVAL = 10 * 1000L
        private const val TIMEOUT_INTERVAL = 15 * 1000L
    }
}

data class DeviceActivityInfo(
    val address: InetAddress,
    val port: Int,
    val lastSeen: Long
)