package com.project.reach.network.controllers

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.project.reach.data.local.IdentityManager
import com.project.reach.domain.contracts.IWifiController
import com.project.reach.network.discovery.HeartBeatDiscoveryHandler
import com.project.reach.network.model.DeviceInfo
import com.project.reach.network.model.Packet
import com.project.reach.network.transport.DataInputChannel
import com.project.reach.network.transport.DataOutputChannel
import com.project.reach.network.transport.NetworkTransport
import com.project.reach.network.transport.TCPTransport
import com.project.reach.network.transport.UDPTransport
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
import java.net.Inet4Address
import java.net.InetAddress
import java.util.UUID

class WifiController(
    private val context: Context,
    private val udpTransport: UDPTransport,
    private val tcpTransport: TCPTransport,
    identityManager: IdentityManager
): IWifiController {
    private var network: Network? = null
    private var localIpAddress: InetAddress? = null
    private val _newDevice = MutableSharedFlow<DeviceInfo>(replay = 0, extraBufferCapacity = 64)
    override val newDevices = _newDevice.asSharedFlow()

    private val myUserId = identityManager.userId
    private val myUsername = identityManager.username

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var tcpObserverJob: Job? = null
    private var udpObserverJob: Job? = null

    private val _packets = MutableSharedFlow<Packet>(replay = 0, extraBufferCapacity = 64)
    override val packets = _packets.asSharedFlow()

    private val connectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

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

    private val networkCallback = object: ConnectivityManager.NetworkCallback() {
        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            debug("Connected to WiFi network")
            linkProperties.getIpAddress()?.let { ip ->
                this@WifiController.network = network
                this@WifiController.localIpAddress = ip
                udpTransport.start(ip, network)
                tcpTransport.start(ip, network)
                wifiDiscoveryHandler.start()
            }
        }

        override fun onLost(network: Network) {
            wifiDiscoveryHandler.stop()
            clearFoundDevices()
            udpTransport.close()
            tcpTransport.close()
        }
    }

    private var isStarted = false
    val networkRequest: NetworkRequest = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .build()

    override fun start() {
        if (isStarted) {
            debug("WifiController already started")
            return
        }

        isStarted = true
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
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

    override suspend fun getDataInputChannel(uuid: UUID): DataInputChannel? {
        return localIpAddress?.let { localAddress ->
            val address = try {
                wifiDiscoveryHandler.resolvePeerAddress(uuid.toString())
            } catch (_: NoSuchElementException) {
                debug("Couldn't create data channel: peer not found")
                return null
            }
            DataInputChannel(address, localAddress)
        }
    }

    override suspend fun getDataOutputChannel(uuid: UUID, port: Int): DataOutputChannel? {
        return network?.let { network ->
            val address = try {
                wifiDiscoveryHandler.resolvePeerAddress(uuid.toString())
            } catch (_: NoSuchElementException) {
                debug("Couldn't create data channel: peer not found")
                return null
            }
            DataOutputChannel(address, port, network)
        }
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
        network?.let {
            val bytes = packet.serialize()
            val transport = if (stream) tcpTransport else udpTransport
            return transport.send(bytes, ip)
        }
        return false
    }

    override fun stop() {
        if (!isStarted) {
            debug("WiFi controller stopped before start")
            return
        }

        isStarted = false
        connectivityManager.unregisterNetworkCallback(networkCallback)
        wifiDiscoveryHandler.stop()
        clearFoundDevices()

        tcpObserverJob?.cancel()
        udpObserverJob?.cancel()
    }

    private fun clearFoundDevices() {
        _foundDevices.update { emptyList() }
        wifiDiscoveryHandler.clear()
    }

    private fun LinkProperties.getIpAddress(): InetAddress? {
        return linkAddresses
            .map { it.address }
            .firstOrNull { it is Inet4Address && !it.isLoopbackAddress }
    }
}