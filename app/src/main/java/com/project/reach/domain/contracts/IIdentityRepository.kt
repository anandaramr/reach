package com.project.reach.domain.contracts

/**
 * Handles user identity creation and retrieval
 */
interface IIdentityRepository {

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
    fun getUserId(): String

    /**
     * Retrieves username. Return `null` if it doesn't exist
     *
     * Can be used to check if user hasn't onboarded
     */
    fun getUsername(): String?

    /**
     * Updates username
     */
    fun updateUsername(username: String)
}