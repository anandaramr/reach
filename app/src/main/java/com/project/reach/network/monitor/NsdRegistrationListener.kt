package com.project.reach.network.monitor

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import com.project.reach.util.debug

class NsdRegistrationListener: NsdManager.RegistrationListener {
    override fun onRegistrationFailed(
        serviceInfo: NsdServiceInfo?,
        errorCode: Int
    ) {
        debug("Registration failed: $errorCode")
    }

    override fun onServiceRegistered(serviceInfo: NsdServiceInfo?) {
        debug("Service registered")
    }

    override fun onServiceUnregistered(serviceInfo: NsdServiceInfo?) {
        debug("Service unregistered")
    }

    override fun onUnregistrationFailed(
        serviceInfo: NsdServiceInfo?,
        errorCode: Int
    ) {
        debug("Unregistration failed")
    }
}