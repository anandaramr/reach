package com.project.reach.network.discovery

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import com.project.reach.data.local.IdentityManager
import com.project.reach.network.model.DeviceInfo
import com.project.reach.network.monitor.NsdDiscoveryListener
import com.project.reach.network.monitor.NsdRegistrationListener
import com.project.reach.network.monitor.NsdResolveListener
import com.project.reach.network.transport.NetworkTransport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.net.InetAddress
import java.util.UUID
import kotlin.collections.plus

/**
 * Manages discovery and resolution of available REACH services
 * using Android NSD API
 *
 * Should call [close] during cleanup
 */
class NsdDiscoveryController(
    private val context: Context,
    identityManager: IdentityManager,
) {
    private val username = identityManager.getUsernameIdentity()
    private val uuid = UUID.fromString(identityManager.getUserUUID())

    private val multicastLock by lazy {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiManager.createMulticastLock("ReachDiscovery").apply {
            setReferenceCounted(true)
        }
    }

    private val nsdManager by lazy {
        context.getSystemService(Context.NSD_SERVICE) as NsdManager
    }

    // TODO: Use discovery protocol over UDP on found devices
    private val _foundServices = MutableStateFlow<List<DeviceInfo>>(emptyList())

    /**
     * Exposes a read-only [kotlinx.coroutines.flow.StateFlow] of discovered services.
     *
     * Each entry is a [Pair] where the first value is the service `uuid` and the second is the `username`
     */
    val foundServices: StateFlow<List<DeviceInfo>> = _foundServices.asStateFlow()

    private val registrationListener = NsdRegistrationListener()

    init {
        acquireMulticastLock()
        registerService()
    }

    private fun registerService() {
        // TODO limit username chars to <=26 chars and validate username format
        val serviceInfo = NsdServiceInfo().apply {
            serviceName = "$uuid:$username"
            serviceType = SERVICE_TYPE
            port = NetworkTransport.Companion.PORT
        }

        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
    }

    private fun unregisterService() {
        runCatching { nsdManager.unregisterService(registrationListener) }
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
     * invokes [onResolved] when its host address is available
     *
     * Example usage:
     * ```
     * resolvePeerAddress(uuid) { host ->
     *  Log.d(TAG, "Device address is $host")
     * }
     * ```
     */
    fun resolvePeerAddress(uuid: UUID, onResolved: (host: InetAddress) -> Unit): Boolean {
        resolveCallback = onResolved
        if (!serviceInfoMap.contains(uuid)) return false

        nsdManager.resolveService(serviceInfoMap[uuid], resolveListener)
        return true
    }

    private val serviceInfoMap: MutableMap<UUID, NsdServiceInfo> = mutableMapOf()
    private var resolveCallback: ((InetAddress) -> Unit)? = null
    private val resolveListener = NsdResolveListener { ip ->
        resolveCallback?.invoke(ip)
        resolveCallback = null
    }

    /**
     * Should be called during cleanup
     */
    fun close() {
        releaseMulticastLock()
        stopDiscovery()
        unregisterService()
    }

    private fun acquireMulticastLock() {
        multicastLock.acquire()
    }

    private fun releaseMulticastLock() {
        if (multicastLock.isHeld) {
            multicastLock.release()
        }
    }

    private fun onFoundService(serviceInfo: NsdServiceInfo) {
        val parts = parseServiceName(serviceInfo)
        if (parts.size != 2) return

        val uuid = UUID.fromString(parts[0])
        val username = parts[1]

        if (uuid == this.uuid) return
        if (serviceInfoMap.contains(uuid)) return

        _foundServices.update {
            it + DeviceInfo(uuid, username)
        }

        serviceInfoMap.put(uuid, serviceInfo)
    }

    private fun onLostService(serviceInfo: NsdServiceInfo) {
        val parts = parseServiceName(serviceInfo)
        if (parts.size != 2) return

        val foundUuid = UUID.fromString(parts[0])

        _foundServices.update {
            it.filter { uuid ->
                uuid != foundUuid
            }
        }

        serviceInfoMap.remove(foundUuid)
    }

    private fun parseServiceName(serviceInfo: NsdServiceInfo): List<String> {
        return serviceInfo.serviceName.split(':')
    }

    companion object {
        private const val SERVICE_TYPE = "_reach._tcp"
    }
}