package com.project.reach.data.respository

import com.project.reach.domain.contracts.INetworkController
import com.project.reach.domain.contracts.INetworkRepository
import com.project.reach.network.transport.NetworkTransport

class NetworkRepository(
    private val networkController: INetworkController,
    private val udpTransport: NetworkTransport,
    private val tcpTransport: NetworkTransport,
): INetworkRepository {

    override val networkState = networkController.networkState
    override val foundDevices = networkController.foundDevices

    override fun startDiscovery() {
        udpTransport.start()
        tcpTransport.start()
        networkController.startDiscovery()
    }

    override fun stopDiscovery() {
        networkController.stopDiscovery()
        udpTransport.close()
        tcpTransport.close()
    }

    override fun release() {
        stopDiscovery()
        networkController.release()
    }
}