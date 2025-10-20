package com.project.reach.network.monitor

import android.net.ConnectivityManager
import android.net.Network

class NetworkCallback(
    val onConnectionAvailable: () -> Unit,
    val onConnectionLost: () -> Unit
): ConnectivityManager.NetworkCallback() {
    override fun onLost(network : Network) {
        onConnectionLost()
    }

    override fun onAvailable(network: Network) {
        onConnectionAvailable()
    }
}