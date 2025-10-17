package com.project.reach.ui.components.login

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel() : ViewModel() {
    private val _uiState = MutableStateFlow(LoginState())
    val uiState = _uiState.asStateFlow()

    private fun updateUsername (text: String) {
        _uiState.update { currentState ->
            currentState.copy( user = text)
        }
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