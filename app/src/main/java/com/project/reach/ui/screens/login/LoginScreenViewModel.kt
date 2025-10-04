package com.project.reach.ui.screens.login

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginScreenViewModel() : ViewModel() {
    private val _uiState = MutableStateFlow(LoginScreenState())
    val uiState = _uiState.asStateFlow()

    private fun updateUsername (text: String) {
        _uiState.value.user.username = text
    }

    private fun userLogin () {
        // Login the user :-| idk..
    }

    fun onInputChange(text: String) {
        updateUsername(text)
    }

    fun login() {
        userLogin()
    }
}