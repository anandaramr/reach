package com.project.reach.data.respository

import com.project.reach.domain.contracts.INetworkController
import com.project.reach.domain.contracts.INetworkRepository
import com.project.reach.network.transport.NetworkTransport

class NetworkRepository(
    private val networkController: INetworkController,
): INetworkRepository {

    override val foundDevices = networkController.foundDevices

    override fun startDiscovery() {
        networkController.startDiscovery()
    }

    override fun stopDiscovery() {
        networkController.stopDiscovery()
    }

    override fun release() {
        stopDiscovery()
        networkController.release()
    }
}