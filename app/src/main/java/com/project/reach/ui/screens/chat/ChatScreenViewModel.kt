package com.project.reach.ui.screens.chat

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class ChatScreenViewModel() : ViewModel() {

    private val _uiState = MutableStateFlow(ChatScreenState())
    val uiState: StateFlow<ChatScreenState> = _uiState.asStateFlow()

    fun updateMessageText(text: String) {
        _uiState.update { currentState ->
            currentState.copy(messageText = text)
        }
    }

    fun updateMessageList(text: String) {
        _uiState.update { currentState ->
            currentState.copy(
                messageText = "",
                messageList = listOf(Message(text,true)) + currentState.messageList
            )
        }
    }

    fun onInputChange(text: String) {
        updateMessageText(text)
    }

    fun sendMessage(text: String) {
        updateMessageList(text)
    }

    init {
        _uiState.update { currentState ->
            currentState.copy(
                messageList = listOf(
                    Message("Hello!", isFromSelf = false),
                    Message("Hi! How are you?", isFromSelf = true),
                    Message("Hi! How are you?", isFromSelf = true),
                    Message("I’m good, thanks!", isFromSelf = false),
                    Message("Compose is fun!", isFromSelf = true),
                    Message("You can add as many messages as you want.", isFromSelf = false)
                ),
                user = UserPreview("Raman")
            )
        }
    }
}

