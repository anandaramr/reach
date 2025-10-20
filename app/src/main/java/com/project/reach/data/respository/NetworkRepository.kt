package com.project.reach.data.respository

import com.project.reach.domain.contracts.INetworkRepository
import com.project.reach.domain.contracts.IWifiController
import com.project.reach.domain.models.NotificationEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class NetworkRepository(
    private val wifiController: IWifiController,
): INetworkRepository {

    override val isWifiActive = wifiController.isActive
    override val foundDevices = wifiController.foundDevices

    override fun startDiscovery() {
        wifiController.startDiscovery()
    }

    override fun stopDiscovery() {
        wifiController.stopDiscovery()
    }

    override fun release() {
        wifiController.close()
    }
}