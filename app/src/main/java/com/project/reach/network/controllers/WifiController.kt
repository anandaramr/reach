package com.project.reach.network.controllers

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.project.reach.core.exceptions.UnknownSourceException
import com.project.reach.data.local.IdentityManager
import com.project.reach.domain.contracts.IWifiController
import com.project.reach.network.contracts.DiscoveryHandler
import com.project.reach.network.discovery.UdpDiscoveryHandler
import com.project.reach.network.model.DeviceInfo
import com.project.reach.network.model.Packet
import com.project.reach.network.model.PacketWithSource
import com.project.reach.network.monitor.NetworkCallback
import com.project.reach.network.transport.NetworkTransport
import com.project.reach.util.debug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.util.UUID

class WifiController(
    private val context: Context,
    private val udpTransport: NetworkTransport,
    identityManager: IdentityManager
): IWifiController {

    private val _isActive = MutableStateFlow(false)
    override val isActive = _isActive.asStateFlow()

    private val username = identityManager.getUsernameIdentity().toString()
    private val uuid = identityManager.getUserUUID().toString()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _packets = MutableSharedFlow<Packet>(replay = 0, extraBufferCapacity = 64)
    override val packets = _packets.asSharedFlow()

    private val connectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private val networkCallback = NetworkCallback(
        onConnectionAvailable = { _isActive.value = isConnectedToWifiAP() },
        onConnectionLost = { _isActive.value = false }
    )

    private val discoveryPackets =
        MutableSharedFlow<PacketWithSource>(replay = 0, extraBufferCapacity = 64)
    private val udpDiscoveryHandler: DiscoveryHandler = UdpDiscoveryHandler(
        userId = uuid,
        username = username,
        packets = discoveryPackets,
        sendPacket = ::sendPacket,
        onFound = { id, username ->
            try {
                val uuid = UUID.fromString(id)
                _foundDevices.value += DeviceInfo(uuid, username)
                debug("Found $username:$uuid")
                true
            } catch (e: IllegalArgumentException) {
                debug("Received invalid uuid")
                false
            }
        },
        onLost = { id ->
            debug("Lost $id")
            _foundDevices.update { devices ->
                devices.filter { device -> device.uuid.toString() != id }
            }
        }
    )

    private val _foundDevices = MutableStateFlow<List<DeviceInfo>>(emptyList())
    override var foundDevices = _foundDevices.asStateFlow()

    init {
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    override fun startDiscovery() {
        scope.launch {
            isActive.collect { active ->
                if (active) {
                    debug("Restarting")
                    udpDiscoveryHandler.start()
                } else {
                    debug("Stopping")
                    stopDiscovery()
                }
            }
        }

        udpTransport.listen { clientIp, bytes ->
            try {
                val packet = Packet.deserialize(bytes)
                if (isDiscoveryPacket(packet)) {
                    discoveryPackets.emit(PacketWithSource(packet, clientIp))
                } else {
                    _packets.emit(Packet.deserialize(bytes))
                }
            } catch (_: IllegalArgumentException) {
            }
        }
    }

    private fun isDiscoveryPacket(packet: Packet): Boolean {
        return packet is Packet.Hello || packet is Packet.Heartbeat || packet is Packet.GoodBye
    }

    override suspend fun send(uuid: UUID, packet: Packet): Boolean {
        try {
            val ipAddress = udpDiscoveryHandler.resolvePeerAddress(uuid.toString())
            sendPacket(ipAddress, packet)
            return true
        } catch (e: NoSuchElementException) {
            debug(e.message.toString())
            return false
        }
    }

    private fun sendPacket(ip: InetAddress, packet: Packet) {
        val bytes = packet.serialize()
        scope.launch {
            udpTransport.send(bytes, ip)
        }
    }

    override fun stopDiscovery() {
        clearFoundDevices()
        udpDiscoveryHandler.stop()
    }

    private fun clearFoundDevices() {
        _foundDevices.update {
            emptyList()
        }
        udpDiscoveryHandler.clear()
    }

    private fun isConnectedToWifiAP(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    override fun close() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
        udpDiscoveryHandler.close()
        udpTransport.close()
    }
}