package com.project.reach.ui.screens.discover
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.reach.data.respository.MessageRepository
import com.project.reach.domain.contracts.IMessageRepository
import com.project.reach.domain.contracts.INetworkRepository
import com.project.reach.network.model.DeviceInfo
import com.project.reach.util.debug
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel @Inject constructor(
   private val networkRepository: INetworkRepository,
   private val messageRepository: IMessageRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(DiscoveryState())
    val uiState : StateFlow<DiscoveryState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            networkRepository.foundDevices.collect { devices ->
                _uiState.update {
                    it.copy(
                        devices.map { device -> Peer(device.username, device.uuid.toString()) }
                    )
                }
            }
        }
    }

    fun saveContact(peer: Peer) {
        viewModelScope.launch {
            messageRepository.saveNewContact(peer.uuid, peer.username)
        }
    }
}