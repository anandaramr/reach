package com.project.reach.data.local

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID
import androidx.core.content.edit

class IdentityManager (
    private val context: Context
) {
    companion object {
        const val UUID_KEY = "uuid_identity"
        const val USERNAME_KEY = "username"
    }

    val sharedPrefs: SharedPreferences by lazy {
        context.getSharedPreferences("reach_prefs", Context.MODE_PRIVATE)
    }

    fun getUserUUID(): String? {
        return sharedPrefs.getString(UUID_KEY, "")
    }

    fun getUsernameIdentity(): String? {
        return sharedPrefs.getString(USERNAME_KEY, "")
    }

    fun createUserUUID(): String {
        val newUUID = UUID.randomUUID().toString()
        sharedPrefs.edit { putString(UUID_KEY, newUUID) }
        return newUUID
    }

    fun updateUsernameIdentity(username: String) {
        sharedPrefs.edit { putString(USERNAME_KEY, username) }
    }
}