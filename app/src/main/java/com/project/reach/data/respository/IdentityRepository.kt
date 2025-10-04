package com.project.reach.data.respository

import android.content.Context
import com.project.reach.data.local.IdentityManager
import javax.inject.Inject

class IdentityRepository @Inject constructor(
    private val context: Context
): IIdentityRepository {

    private val preferences by lazy {
        IdentityManager(context)
    }

    override fun getIdentity(): String {
        val uuid = preferences.getUserUUID()
        return if (uuid?.isBlank() == false) {
            uuid
        } else {
            preferences.createUserUUID()
        }
    }

    override fun getUsername(): String? {
        return preferences.getUsernameIdentity()
    }

    override fun updateUsername(username: String) {
        preferences.updateUsernameIdentity(username)
    }
}

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