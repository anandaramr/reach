package com.project.reach.data.respository

import com.project.reach.data.local.IdentityManager
import com.project.reach.domain.contracts.IIdentityRepository
import javax.inject.Inject

class IdentityRepository @Inject constructor(
    private val identityManager: IdentityManager
): IIdentityRepository {

    override fun isOnboardingRequired(): Boolean {
        return identityManager.getUserUUID()?.isBlank() != false
    }

    override fun getUserId(): String {
        val uuid = identityManager.getUserUUID()
        return if (uuid?.isBlank() == false) {
            uuid
        } else {
            identityManager.createUserUUID()
        }
    }

    override fun getUsername(): String? {
        return identityManager.getUsernameIdentity()
    }

    override fun updateUsername(username: String) {
        if (!username.matches(Regex(USERNAME_REGEX))) {
            throw IllegalArgumentException("Username should only contain alphabets, numbers, underscores or dots")
        }

        if (username.length > 24) {
            throw IllegalArgumentException("Username should have at most 24 characters")
        }

        identityManager.updateUsernameIdentity(username)

        // create UUID if it doesn't exist
        getUserId()
    }

    companion object {
        private const val USERNAME_REGEX = "^[0-9a-zA-Z_.]+$"
    }
}