package com.project.reach.domain.contracts

import com.project.reach.domain.models.NotificationEvent
import com.project.reach.network.model.DeviceInfo
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository for managing app-level operations
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
     * List of [StateFlow] objects holding the `uuid` and `username` of the users
     */
    val foundDevices: StateFlow<List<DeviceInfo>>

    /**
     * Start discovery process to find users
     *
     * The list of discovered users are found in [foundDevices]
     */
    fun startDiscovery()

    /**
     * Stops discovery process
     */
    fun stopDiscovery()

    /**
     * Releases memory. Should be called during cleanup
     */
    fun release()
}