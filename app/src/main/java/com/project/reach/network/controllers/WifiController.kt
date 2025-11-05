package com.project.reach.network.controllers

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.project.reach.data.local.IdentityManager
import com.project.reach.domain.contracts.IWifiController
import com.project.reach.network.discovery.HeartBeatDiscoveryHandler
import com.project.reach.network.model.DeviceInfo
import com.project.reach.network.model.Packet
import com.project.reach.network.monitor.NetworkCallback
import com.project.reach.network.transport.NetworkTransport
import com.project.reach.util.debug
import com.project.reach.util.toUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.util.UUID
import java.util.zip.DataFormatException

class WifiController(
    private val context: Context,
    private val udpTransport: NetworkTransport,
    private val tcpTransport: NetworkTransport,
    identityManager: IdentityManager
): IWifiController {

    private val _isActive = MutableStateFlow(false)
    override val isActive = _isActive.asStateFlow()

    private val _newDevice = MutableSharedFlow<DeviceInfo>(replay = 0, extraBufferCapacity = 64)
    override val newDevices = _newDevice.asSharedFlow()

    private val username = identityManager.getUsernameIdentity().toString()
    private val uuid = identityManager.getUserUUID().toString()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var networkStateJob: Job? = null
    private var tcpObserverJob: Job? = null
    private var udpObserverJob: Job? = null

    private val _packets = MutableSharedFlow<Packet>(replay = 0, extraBufferCapacity = 64)
    override val packets = _packets.asSharedFlow()

    private val connectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private val networkCallback = NetworkCallback(
        onConnectionAvailable = { _isActive.value = isConnectedToWifiAP() },
        onConnectionLost = { _isActive.value = false }
    )

    private val wifiDiscoveryHandler = HeartBeatDiscoveryHandler(
        userId = uuid,
        username = username,
        sendPacket = { ip, packet ->
            scope.launch {
                sendPacket(ip, packet, stream = false)
            }
        },
        onFound = { peerId, username ->
            try {
                val uuid = peerId.toUUID()
                val device = DeviceInfo(uuid, username)
                _foundDevices.value += device

                scope.launch { _newDevice.emit(device) }
                true
            } catch (_: IllegalArgumentException) {
                false
            }
        },
        onLost = { id ->
            _foundDevices.update { devices ->
                devices.filter { device -> device.uuid.toString() != id }
            }
        }
    )

    private val _foundDevices = MutableStateFlow<List<DeviceInfo>>(emptyList())
    override var foundDevices = _foundDevices.asStateFlow()

    // This variable only signifies that the user has
    // attempted to start discovery not whether discovery
    // has actually started. So this variable should not
    // changed elsewhere
    private var isDiscoveryStartedByUser = false

    init {
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
        scope.launch {
            isActive.collect { active ->
                if (isDiscoveryStartedByUser) {
                    if (active) {
                        start()
                    } else {
                        stop()
                    }
                }
            }
        }
    }

    override fun startDiscovery() {
        if (isDiscoveryStartedByUser) return
        isDiscoveryStartedByUser = true
        start()
    }

    private fun start() {
        if (isActive.value) wifiDiscoveryHandler.start()
        udpObserverJob = observeTransport(udpTransport, "UDP")
        tcpObserverJob = observeTransport(tcpTransport, "TCP")
    }

    private fun observeTransport(transport: NetworkTransport, transportName: String): Job {
        return scope.launch {
            transport.incomingPackets.collect { packet ->
                val clientIp = packet.address
                val bytes = packet.payload
                try {
                    val packet = Packet.deserialize(bytes)
                    wifiDiscoveryHandler.handleIncomingPacket(clientIp, packet)
                    _packets.emit(packet)
                } catch (e: IllegalArgumentException) {
                    debug(e.toString())
                } catch (e: DataFormatException) {
                    debug("$transportName received faulty packet")
                    debug(e.toString())
                }
            }
        }
    }

    override suspend fun sendDatagram(uuid: UUID, packet: Packet): Boolean {
        try {
            val ipAddress = wifiDiscoveryHandler.resolvePeerAddress(uuid.toString())
            return sendPacket(ipAddress, packet, stream = false)
        } catch (e: NoSuchElementException) {
            debug(e.message.toString())
            return false
        }
    }

    override suspend fun sendStream(uuid: UUID, packet: Packet): Boolean {
        try {
            val ipAddress = wifiDiscoveryHandler.resolvePeerAddress(uuid.toString())
            return sendPacket(ipAddress, packet, stream = true)
        } catch (e: NoSuchElementException) {
            debug(e.message.toString())
            return false
        }
    }

    private suspend fun sendPacket(ip: InetAddress, packet: Packet, stream: Boolean): Boolean {
        val bytes = packet.serialize()
        return if (!stream) udpTransport.send(bytes, ip)
        else tcpTransport.send(bytes, ip)
    }

    override fun stopDiscovery() {
        if (!isDiscoveryStartedByUser) return
        isDiscoveryStartedByUser = false
        stop()
    }

    private fun stop() {
        clearFoundDevices()
        networkStateJob?.cancel()
        tcpObserverJob?.cancel()
        udpObserverJob?.cancel()
        wifiDiscoveryHandler.stop()
    }

    private fun clearFoundDevices() {
        _foundDevices.update { emptyList() }
        wifiDiscoveryHandler.clear()
    }

    private fun isConnectedToWifiAP(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    override fun close() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}