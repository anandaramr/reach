package com.project.reach.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.reach.data.respository.IIdentityRepository
import com.project.reach.ui.utils.UIEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val identityRepository: IIdentityRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeScreenState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UIEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            val username = identityRepository.getUsername()
            if (username?.isBlank() != false) {
                _uiState.update { it.copy(needsOnboarding = true) }
                return@launch
            }

            _uiState.update { it.copy(
                userId = identityRepository.getIdentity(),
                username = username
            ) }
        }
    }

    fun onInputChange (text: String) {
        _uiState.update { currentState ->
            currentState.copy(username = text)
        }
    }

    fun completeOnboarding(): Boolean {
        val username = _uiState.value.username

        if (username.isBlank()) {
            viewModelScope.launch {
                _uiEvent.emit(UIEvent.Error("Username cannot be empty"))
            }
            return false
        }

        identityRepository.updateUsername(username)
        _uiState.update { it.copy(username = username, needsOnboarding = false) }
        return true
    }
}