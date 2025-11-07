package com.project.reach.ui.screens.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.reach.domain.contracts.IMessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val messageRepository: IMessageRepository,

): ViewModel() {
    private val _uiState = MutableStateFlow(ChatScreenState())
    val uiState: StateFlow<ChatScreenState> = _uiState.asStateFlow()
    private val peerId: String = savedStateHandle["peerId"]?: ""

    val message = messageRepository.getMessagesPaged(peerId);

    private fun updateMessageText(text: String) {
        _uiState.update { currentState ->
            currentState.copy(messageText = text)
        }
    }
    fun onInputChange(text: String) {
        updateMessageText(text)
        messageRepository.emitTyping(_uiState.value.peerId)
    }

    fun sendMessage(text: String) {
        updateMessageText("")
        viewModelScope.launch {
            messageRepository.sendMessage(_uiState.value.peerId, text)
        }
    }

    private suspend fun updateUserState(peerId: String) {
        val username = messageRepository.getUsername(peerId).first()
        _uiState.update {
            it.copy(peerName = username)
        }
    }

    fun initializeChat() {
        viewModelScope.launch {
            messageRepository.isTyping(peerId).collect { typeStatus ->
                _uiState.update { it.copy( isTyping = typeStatus)}
            }
        }
        _uiState.update { it.copy(peerId = peerId) }
        viewModelScope.launch {
            updateUserState(peerId = peerId)
        }

    }
}

