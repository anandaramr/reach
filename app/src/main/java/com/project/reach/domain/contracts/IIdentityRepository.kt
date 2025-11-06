package com.project.reach.domain.contracts

import kotlinx.coroutines.flow.StateFlow

/**
 * Handles user identity creation and retrieval
 */
interface IIdentityRepository {
    val userId: StateFlow<String>
    val username: StateFlow<String>

    /**
     * Checks whether user is logging in for the first time.
     * Use this to check whether user needs to be onboarded
     */
    fun isOnboardingRequired(): Boolean

    /**
     * Retrieves identity key from `DataStore` if it exists
     *
     * Return new identity key if it doesn't already exist
     */
    @Deprecated("Use the userId StateFlow instead")
    fun getUserId(): String

    /**
     * Retrieves username. Return `null` if it doesn't exist
     *
     * Can be used to check if user hasn't onboarded
     */
    @Deprecated("Use the username StateFlow instead")
    fun getUsername(): String?

    /**
     * Updates username
     */
    fun updateUsername(username: String)
}