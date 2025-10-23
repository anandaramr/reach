package com.project.reach.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.reach.domain.contracts.IMessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    messageRepository: IMessageRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeScreenState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            messageRepository.getMessagesPreview().collect { messagePreviews ->
                _uiState.update {
                    it.copy(chatList = messagePreviews)
                }
            }
        }
    }

}