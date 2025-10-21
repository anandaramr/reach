package com.project.reach.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.reach.domain.contracts.IIdentityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val identityRepository: IIdentityRepository
): ViewModel() {

    private val _username = MutableStateFlow("")
    val username = _username.asStateFlow()

    private val _errors = MutableSharedFlow<String>()
    val errors = _errors.asSharedFlow()

    fun onUsernameChange(newUsername: String) {
        _username.value = newUsername
    }

    fun submit(): Boolean {
        try {
            identityRepository.updateUsername(_username.value.trim())
            return true
        } catch (e: IllegalArgumentException) {
            viewModelScope.launch {
                _errors.emit(e.message ?: "Error")
            }
            return false
        }
    }

}