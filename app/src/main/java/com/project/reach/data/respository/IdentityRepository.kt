package com.project.reach.data.respository

import com.project.reach.data.local.IdentityManager
import com.project.reach.domain.contracts.IIdentityRepository
import javax.inject.Inject

class IdentityRepository @Inject constructor(
    private val identityManager: IdentityManager
): IIdentityRepository {
    override val userId = identityManager.userId
    override val username = identityManager.username

    override fun isOnboardingRequired(): Boolean {
        return identityManager.needsOnboarding()
    }

    @Deprecated("Use the userId StateFlow instead")
    override fun getUserId(): String {
        return identityManager.userId.value
    }

    @Deprecated("Use the username StateFlow instead")
    override fun getUsername(): String? {
        return identityManager.username.value
    }

    override fun updateUsername(username: String) {
        val trimmedUsername = username.trim()
        if (!trimmedUsername.matches(Regex(USERNAME_REGEX))) {
            throw IllegalArgumentException("Username should contain only alphabets, numbers, underscores or dots")
        }

        if (trimmedUsername.length > 24) {
            throw IllegalArgumentException("Username should have at most 24 characters")
        }

        identityManager.updateUsername(trimmedUsername)
    }

    companion object {
        private const val USERNAME_REGEX = "^[0-9a-zA-Z_.]+$"
    }
}