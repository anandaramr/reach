package com.project.reach.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class IdentityManager(
    private val context: Context
) {
    companion object {
        const val UUID_KEY = "uuid_identity"
        const val USERNAME_KEY = "username"
    }

    val sharedPrefs: SharedPreferences by lazy {
        context.getSharedPreferences("reach_prefs", Context.MODE_PRIVATE)
    }

    private val _userId: MutableStateFlow<String> = MutableStateFlow(getUserId() ?: createNewUserID())
    val userId = _userId.asStateFlow()

    private val _username: MutableStateFlow<String> = MutableStateFlow(getUsername() ?: "")
    val username = _username.asStateFlow()

    fun needsOnboarding(): Boolean {
        return getUsername() == null
    }

    @Deprecated("Use userId StateFlow instead")
    fun getUserUUID(): String? {
        return sharedPrefs.getString(UUID_KEY, null)
    }

    private fun getUserId(): String? {
        return sharedPrefs.getString(UUID_KEY, null)
    }

    @Deprecated("Use username StateFlow instead")
    fun getUsernameIdentity(): String? {
        return sharedPrefs.getString(USERNAME_KEY, null)
    }

    private fun getUsername(): String? {
        return sharedPrefs.getString(USERNAME_KEY, null)
    }

    private fun createNewUserID(): String {
        if (getUserId() != null) {
            throw IllegalStateException("ID already created. Cannot create duplicate ID")
        }

        val newUUID = UUID.randomUUID().toString()
        sharedPrefs.edit { putString(UUID_KEY, newUUID) }
        return newUUID
    }

    fun updateUsername(username: String) {
        _username.value = username
        sharedPrefs.edit { putString(USERNAME_KEY, username) }
    }
}