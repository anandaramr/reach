package com.project.reach.domain.contracts

import kotlinx.coroutines.flow.StateFlow

/**
 * Handles user identity creation and retrieval
 */
interface IIdentityRepository {
    val userId: String
    val username: StateFlow<String>

    /**
     * Checks whether user is logging in for the first time.
     * Use this to check whether user needs to be onboarded
     */
    fun isOnboardingRequired(): Boolean

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