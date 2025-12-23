package com.project.reach.domain.contracts

import com.project.reach.domain.models.NetworkState
import com.project.reach.network.model.DeviceInfo
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository for managing app-level operations
 */
interface INetworkRepository {

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