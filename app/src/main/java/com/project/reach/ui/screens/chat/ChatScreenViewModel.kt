package com.project.reach.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.reach.data.local.entity.MessageEntity
import com.project.reach.domain.contracts.IMessageRepository
import com.project.reach.util.debug
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
    private val messageRepository: IMessageRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatScreenState())
    val uiState: StateFlow<ChatScreenState> = _uiState.asStateFlow()
    fun updateMessageText(text: String) {
        _uiState.update { currentState ->
            currentState.copy(messageText = text)
        }
    }

    fun onInputChange(text: String) {
        updateMessageText(text)
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

    private suspend fun getMessages() {
        debug(_uiState.value.peerId)
        messageRepository.getMessages(_uiState.value.peerId).collect { messages ->
            _uiState.update { it.copy(messageList = messages.map { msg -> msg.toMessage() }) }
        }
    }

    fun initializeChat(peerId: String) {
        _uiState.update { it.copy(peerId = peerId) }
        viewModelScope.launch {
            updateUserState(peerId = peerId)
            getMessages()
        }
    }

    private fun MessageEntity.toMessage(): Message {
        return Message(
            text = text,
            isFromSelf = !isFromPeer
        )
    }
}

