package com.project.reach.ui.screens.newContact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.reach.domain.contracts.IContactRepository
import com.project.reach.domain.contracts.IIdentityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewContactViewModel @Inject constructor(
    private val contactRepository: IContactRepository,
    private val identityRepository: IIdentityRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NewContactState())
    val uiState = _uiState.asStateFlow()
    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    fun storeUserDataOnLoad(userId: String, username: String){
        _uiState.update {
            it.copy(
                userId = userId,
                username = username,
                nickname = username
            )
        }
    }

    fun addToContact(){
        viewModelScope.launch {
            saveContact()
        }
    }

    private suspend fun saveContact(): Boolean {
        try{
            contactRepository.saveNewContact(_uiState.value.userId, _uiState.value.username, _uiState.value.nickname)
            return true
        }
        catch (e: IllegalArgumentException) {
            viewModelScope.launch {
                _error.emit(e.message?: "Error")
            }
            return false
        }
    }

    fun onNicknameChange(text: String){
        changeNickname(text)
    }

    private fun changeNickname(text: String) {
        _uiState.update {
            it.copy(
                nickname = text
            )
        }
    }

}