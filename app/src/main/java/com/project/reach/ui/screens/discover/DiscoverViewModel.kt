package com.project.reach.ui.screens.discover
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class DiscoverViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DiscoveryState())
    val uiState : StateFlow<DiscoveryState> = _uiState.asStateFlow()

    init {
        _uiState.update { currentState ->
            currentState.copy(
                peerNameList = listOf(
                    PeerName(username = "devika"),
                    PeerName(username = "ram"),
                    PeerName(username = "unni"),
                    PeerName(username = "ananthu"),
                    PeerName(username = "prani"),
                )
            )
        }
    }
}