package com.project.reach.ui.screens.login

import androidx.lifecycle.ViewModel
import com.project.reach.ui.screens.chat.UserPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginScreenViewModel() : ViewModel() {
    private val _uiState = MutableStateFlow(LoginScreenState())
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