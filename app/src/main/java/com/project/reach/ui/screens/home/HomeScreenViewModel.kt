package com.project.reach.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeScreenState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(
                chatList = listOf(
                    ChatItem( username = "Raman", lastMessage = "Wait, did you check the link I sent?"),
                    ChatItem( username = "Unni", lastMessage = "Got the files, thanks a lot!"),
                    ChatItem( username = "Ananthu", lastMessage = "Let’s meet at the café tomorrow?"),
                    ChatItem( username = "Devika", lastMessage = "Brooo that was hilarious"),
                    ChatItem( username = "Prani", lastMessage = "Ok cool, see you at 7!"),
                )
            ) }
        }
    }
}