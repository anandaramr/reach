package com.project.reach.domain.contracts

/**
 * Handles user identity creation and retrieval
 */
interface IIdentityRepository {

    /**
     * Retrieves identity key from `DataStore` if it exists
     *
     * Return new identity key if it doesn't already exist
     */
    fun getIdentity(): String

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