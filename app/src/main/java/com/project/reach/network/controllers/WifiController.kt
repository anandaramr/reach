package com.project.reach.network.controllers

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.project.reach.domain.contracts.IWifiController
import com.project.reach.network.model.DeviceInfo
import com.project.reach.network.model.Packet
import com.project.reach.network.monitor.NetworkCallback
import com.project.reach.network.transport.NetworkTransport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.util.UUID

class WifiController(
    private val context: Context,
    private val discoveryController: DiscoveryController,
    private val udpTransport: NetworkTransport
): IWifiController {

    private val _isActive = MutableStateFlow(false)
    override val isActive = _isActive.asStateFlow()

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

    override var foundDevices: StateFlow<List<DeviceInfo>>

    init {
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
        foundDevices = discoveryController.foundServices
    }

    override fun startDiscovery() {
        discoveryController.startDiscovery()
        udpTransport.listen { bytes ->
            try {
                _packets.emit(Packet.deserialize(bytes))
            } catch (_: IllegalArgumentException) {
            }
        }
    }

    override fun send(uuid: UUID, packet: Packet): Boolean {
        return discoveryController.resolvePeerAddress(uuid) { ip ->
            sendPacket(ip, packet)
        }
    }

    private fun sendPacket(ip: InetAddress, packet: Packet) {
        val bytes = packet.serialize()
        scope.launch {
            udpTransport.send(bytes, ip)
        }
    }

    override fun stopDiscovery() {
        discoveryController.stopDiscovery()
    }

    private fun isConnectedToWifiAP(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    override fun close() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
        discoveryController.close()
        udpTransport.close()
    }
}