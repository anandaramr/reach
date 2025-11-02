package com.project.reach.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.reach.domain.contracts.IMessageRepository
import com.project.reach.domain.models.MessagePreview
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val messageRepository: IMessageRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeScreenState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            messageRepository.getMessagesPreview().collect { messagePreviews ->
                _uiState.update {
                    it.copy(chatPreview = messagePreviews.map { msg -> msg.toUIMessagePreview() })
                }
            }
        }
    }
    private fun MessagePreview.toUIMessagePreview(): UIMessagePreview {
        return UIMessagePreview(
            userId = userId.toString(),
            username = username,
            lastMessage = lastMessage,
            messageState = messageState,
            timeStamp = timeStamp,
            isTyping = messageRepository.isTyping(userId.toString()).toStateFlow( scope = viewModelScope, initialValue = false)
        )
    }
    private fun <T> Flow<T>.toStateFlow(
        scope: CoroutineScope,
        initialValue: T
    ): StateFlow<T> {
        return this.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(500),
            initialValue = initialValue
        )
    }


}