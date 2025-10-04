package com.project.reach.ui.screens.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class HomeScreenViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeScreenState())
    val uiState = _uiState.asStateFlow()

    private fun changeConnection(text: ConnectionMode) {
        _uiState.update { currentState ->
            currentState.copy(connectionMode = text)
        }
    }

    fun changeConnectionMode(text: ConnectionMode) {
        changeConnection(text)
    }
    private fun updateUsername (text: String) {
        _uiState.update { currentState ->
            currentState.copy( username = text)
        }
    }
    private fun onBoard () {
        _uiState.update { currentState ->
            currentState.copy( userId = "123")
        }
    }

    fun onInputChange(text: String) {
        updateUsername(text)
    }

    fun completeOnboarding() {
        onBoard()
    }
}