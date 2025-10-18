package com.project.reach.data.respository

import com.project.reach.domain.contracts.IAppRepository
import com.project.reach.domain.contracts.IWifiController
import com.project.reach.domain.models.NotificationEvent
import com.project.reach.network.transport.NetworkTransport
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class AppRepository(
    private val wifiController: IWifiController,
    private val udpTransport: NetworkTransport
): IAppRepository {
    private val _notifications = MutableSharedFlow<NotificationEvent>()
    override val notifications = _notifications.asSharedFlow()

    override val isWifiActive = wifiController.isActive

    override fun startUDPServer() {
        udpTransport.listen {  }
    }

    override fun stopUDPServer() {
        udpTransport.close()
    }
}