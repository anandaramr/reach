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
import com.project.reach.network.transport.DataInputChannel
import com.project.reach.network.transport.DataOutputChannel
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

    private val myUserId = identityManager.userId
    private val myUsername = identityManager.username

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
        myUserId = myUserId,
        myUsername = myUsername,
        sendPacket = { ip, packet ->
            scope.launch {
                sendPacketToAddress(ip, packet, stream = false)
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
                debug("Invalid device credentials")
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

    // This variable only signifies that the user has attempted to start discovery
    // not whether discovery has actually started. So this variable should not
    // be changed elsewhere
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
                    if (packet.senderId == myUserId) return@collect // ignore packets from self (during broadcasts)

                    wifiDiscoveryHandler.handleIncomingPacket(clientIp, packet)
                    _packets.emit(packet)
                } catch (e: IllegalArgumentException) {
                    debug("$transportName received faulty packet")
                    debug(e.toString())
                }
            }
        }
    }

    override suspend fun sendDatagram(uuid: UUID, packet: Packet): Boolean {
        return sendPacketToUser(uuid, packet, stream = false)
    }

    override suspend fun sendStream(uuid: UUID, packet: Packet): Boolean {
        return sendPacketToUser(uuid, packet, stream = true)
    }

    override suspend fun getDataInputChannel(uuid: UUID): DataInputChannel {
        val address = wifiDiscoveryHandler.resolvePeerAddress(uuid.toString())
        return DataInputChannel(address)
    }

    override suspend fun getDataOutputChannel(uuid: UUID, port: Int): DataOutputChannel {
        val address = wifiDiscoveryHandler.resolvePeerAddress(uuid.toString())
        return DataOutputChannel(address, port)
    }

    private suspend fun sendPacketToUser(uuid: UUID, packet: Packet, stream: Boolean): Boolean {
        val ipAddress = try {
            wifiDiscoveryHandler.resolvePeerAddress(uuid.toString())
        } catch (e: NoSuchElementException) {
            debug(e.message.toString())
            return false
        }

        return sendPacketToAddress(ipAddress, packet, stream)
    }

    private suspend fun sendPacketToAddress(
        ip: InetAddress, packet: Packet, stream: Boolean
    ): Boolean {
        val bytes = packet.serialize()
        val transport = if (stream) tcpTransport else udpTransport
        return transport.send(bytes, ip)
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