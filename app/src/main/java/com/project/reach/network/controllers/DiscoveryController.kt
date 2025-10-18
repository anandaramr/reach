package com.project.reach.network.controllers

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import com.project.reach.network.monitor.NsdDiscoveryListener
import com.project.reach.network.monitor.NsdRegistrationListener
import com.project.reach.network.monitor.NsdResolveListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.net.DatagramSocket
import java.net.InetAddress

/**
 * Manages discovery and resolution of available REACH services
 * using Android NSD API
 *
 * Should call [close] during cleanup
 */
class DiscoveryController(
    private val context: Context,
    private val uuid: String,
    private val username: String,
) {

    private val nsdManager by lazy {
        context.getSystemService(Context.NSD_SERVICE) as NsdManager
    }

    private val _foundServices = MutableStateFlow<List<Pair<String, String>>>(emptyList())

    /**
     * Exposes a read-only [StateFlow] of discovered services.
     *
     * Each entry is a [Pair] where the first value is the service `uuid` and the second is the `username`
     */
    val foundServices: StateFlow<List<Pair<String, String>>> = _foundServices.asStateFlow()

    init {
        registerService()
    }

    private val registrationListener = NsdRegistrationListener()

    private fun registerService() {
        // TODO limit username chars to <=26 chars and validate username format
        val serviceInfo = NsdServiceInfo().apply {
            serviceName = "$uuid:$username"
            serviceType = SERVICE_TYPE
            port = getAvailablePort()
        }

        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
    }

    private fun unregisterService() {
        nsdManager.unregisterService(registrationListener)
    }

    /**
     * Starts Network Service Discovery
     */
    fun startDiscovery() {
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    private val discoveryListener = NsdDiscoveryListener(
        onFound = ::onFoundService,
        onLost = ::onLostService
    )

    /**
     * Stops discovery process if it is going on
     *
     * NSD is an expensive process and hence should be stopped
     * if not necessary
     */
    fun stopDiscovery() {
        runCatching { nsdManager.stopServiceDiscovery(discoveryListener) }
    }

    /**
     * Resolves the service with the given [uuid] and
     * invokes [onResolved] when its host and port are available
     *
     * Example usage:
     * ```
     * getServiceInfo(uuid) { host, port ->
     *  Log.d(TAG, "Device address is $host:$port")
     * }
     * ```
     */
    fun getServiceInfo(uuid: String, onResolved: (host: InetAddress, port: Int) -> Unit) {
        resolveCallback = onResolved
        nsdManager.resolveService(serviceInfoMap[uuid], resolveListener)
    }

    private val serviceInfoMap: MutableMap<String, NsdServiceInfo> = mutableMapOf()
    private var resolveCallback: ((InetAddress, Int) -> Unit)? = null
    private val resolveListener = NsdResolveListener { ip, port ->
        resolveCallback?.invoke(ip, port)
        resolveCallback = null
    }

    /**
     * Should be called during cleanup
     */
    fun close() {
        stopDiscovery()
        unregisterService()
    }

    private fun onFoundService(serviceInfo: NsdServiceInfo) {
        val parts = parseServiceName(serviceInfo)
        if (parts.size != 2) return

        val uuid = parts[0]
        val username = parts[1]

        if (uuid == this.uuid) return

        _foundServices.update {
            it + Pair(uuid, username)
        }

        serviceInfoMap.put(uuid, serviceInfo)
    }

    private fun onLostService(serviceInfo: NsdServiceInfo) {
        val parts = parseServiceName(serviceInfo)
        if (parts.size != 2) return

        val foundUuid = parts[0]

        _foundServices.update {
            it.filter { (uuid, _) ->
                uuid != foundUuid
            }
        }

        serviceInfoMap.remove(foundUuid)
    }

    private fun parseServiceName(serviceInfo: NsdServiceInfo): List<String> {
        return serviceInfo.serviceName.split(':')
    }

    private fun getAvailablePort(): Int {
        return DatagramSocket(0).localPort
    }

    companion object {
        private const val SERVICE_TYPE = "_reach._tcp"
    }
}