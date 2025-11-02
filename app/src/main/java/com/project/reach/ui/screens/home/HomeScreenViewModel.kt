package com.project.reach.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.project.reach.domain.contracts.IMessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val messageRepository: IMessageRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeScreenState())
    val uiState = _uiState.asStateFlow()
    val messagePreview = messageRepository.getMessagePreviewsPaged().cachedIn(viewModelScope)
    val typingUsers = messageRepository.typingUsers

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