package com.project.reach.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.project.reach.domain.contracts.IIdentityRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val identityRepository: IIdentityRepository,
): ViewModel() {
    private val _uiState = MutableStateFlow(SettingState())
    val uiState = _uiState.asStateFlow()
    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    init {
        _uiState.update {
            it.copy(
                username = identityRepository.username.value,
                userId = identityRepository.userId
            )
        }
    }

    fun updateUsername(): Boolean {
        try{
            identityRepository.updateUsername(_uiState.value.username)
            return true
        }
        catch (e: IllegalArgumentException) {
            viewModelScope.launch {
                _error.emit(e.message?: "Error")
            }
            return false
        }
    }
    fun onUsernameChange(text: String) {
        _uiState.update {
            it.copy(
                username = text
            )
        }
    }
}