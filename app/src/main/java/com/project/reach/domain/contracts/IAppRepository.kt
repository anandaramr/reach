package com.project.reach.domain.contracts

import com.project.reach.domain.models.NotificationEvent
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository for managing main app operations and data
 */
interface IAppRepository {
    /**
     * A [SharedFlow] that emits [NotificationEvent] objects
     *
     * Emits all notification events
     */
    val notifications: SharedFlow<NotificationEvent>

    /**
     * A [StateFlow] that represents current WiFi state
     *
     * Emits `true` if device is connected to a private network
     * via WiFi, otherwise emits `false`
     */
    val isWifiActive: StateFlow<Boolean>

    /**
     * Starts UDP server. Server listens to incoming packets
     */
    fun startUDPServer()

    /**
     * Stops UDP server. Should be called during cleanup
     */
    fun stopUDPServer()
}