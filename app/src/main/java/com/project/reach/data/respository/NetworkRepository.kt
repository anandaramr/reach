package com.project.reach.data.respository

import com.project.reach.domain.contracts.INetworkRepository
import com.project.reach.domain.contracts.IWifiController
import com.project.reach.network.transport.NetworkTransport

// TODO use NetworkController
class NetworkRepository(
    private val wifiController: IWifiController,
    private val udpTransport: NetworkTransport,
    private val tcpTransport: NetworkTransport,
): INetworkRepository {

    override val isWifiActive = wifiController.isActive
    override val foundDevices = wifiController.foundDevices

    override fun startDiscovery() {
        udpTransport.start()
        tcpTransport.start()
        wifiController.startDiscovery()
    }

    override fun stopDiscovery() {
        wifiController.stopDiscovery()
        udpTransport.close()
        tcpTransport.close()
    }

    override fun release() {
        stopDiscovery()
        wifiController.close()
    }
}