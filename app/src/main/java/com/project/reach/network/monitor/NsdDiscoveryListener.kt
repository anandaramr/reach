package com.project.reach.network.monitor

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import com.project.reach.util.debug

class NsdDiscoveryListener(
    private val onFound: (serviceInfo: NsdServiceInfo) -> Unit,
    private val onLost: (serviceInfo: NsdServiceInfo) -> Unit
): NsdManager.DiscoveryListener {
    override fun onDiscoveryStarted(serviceType: String?) {
        debug("Discovery started")
    }

    override fun onDiscoveryStopped(serviceType: String?) {
        debug("Discovery stopped")
    }

    override fun onServiceFound(serviceInfo: NsdServiceInfo) {
        onFound(serviceInfo)
    }

    override fun onServiceLost(serviceInfo: NsdServiceInfo) {
        onLost(serviceInfo)
    }

    override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
        debug("Start discovery failed $errorCode")
    }

    override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
        debug("Stop discovery failed")
    }
}