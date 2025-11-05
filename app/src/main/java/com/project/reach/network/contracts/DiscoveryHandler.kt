package com.project.reach.network.contracts

import com.project.reach.network.model.DeviceInfo
import kotlinx.coroutines.flow.SharedFlow
import java.net.InetAddress

interface DiscoveryHandler {
    /**
     * Starts discovery process if it has not already started
     */
    fun start()

    /**
     * Stops discovery process if it has already started
     */
    fun stop()

    /**
     * Retrieves the IP address of the given user if the user is discoverable
     *
     * Example usage:
     * ```
     * scope.launch {
     *     try {
     *         val ipAddress = resolveAddress(userId)
     *         // use the ip address of the user
     *     } catch (e: NoSuchElementException) {
     *         Log.d(TAG, "User cannot be discovered")
     *     }
     * }
     * ```
     *
     * @param uuid The user ID of the user whose IP address should be fetched
     * @return [InetAddress] of the user
     *
     * @throws NoSuchElementException if user cannot be resolved. This
     * typically occurs when attempting to resolve a user who is
     * not discoverable
     */
    suspend fun resolvePeerAddress(uuid: String): InetAddress

    fun clear()
}