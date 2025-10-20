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

    override fun isOnboardingRequired(): Boolean {
        return preferences.getUserUUID()?.isBlank() != false
    }

    override fun getUserId(): String {
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
        if (!username.matches(Regex(USERNAME_REGEX))) {
            throw IllegalArgumentException("Username should only contain alphabets, numbers, underscores or dots")
        }

        if (username.length > 24) {
            throw IllegalArgumentException("Username should have at most 24 characters")
        }

        preferences.updateUsernameIdentity(username)
    }

    companion object {
        private const val USERNAME_REGEX = "^[0-9a-zA-Z_.]+$"
    }
}