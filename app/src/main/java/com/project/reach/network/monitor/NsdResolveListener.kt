package com.project.reach.network.monitor

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import com.project.reach.util.debug
import java.net.InetAddress

class NsdResolveListener(
    private val onResolved: (ip: InetAddress, port: Int) -> Unit
): NsdManager.ResolveListener {
    override fun onResolveFailed(
        serviceInfo: NsdServiceInfo?,
        errorCode: Int
    ) {
        debug("Resolve failed $errorCode")
    }

    override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
        val ip = serviceInfo.host
        val port = serviceInfo.port
        onResolved(ip, port)
    }
}