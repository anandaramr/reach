package com.project.reach.data.respository

import android.content.Context
import com.project.reach.data.local.IdentityManager
import com.project.reach.domain.contracts.IIdentityRepository
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