package com.project.reach.ui.screens.contacts

import com.project.reach.ui.screens.discover.DiscoveryState
import com.project.reach.ui.screens.discover.Peer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.reach.data.respository.ContactRepository
import com.project.reach.data.respository.MessageRepository
import com.project.reach.domain.contracts.IContactRepository
import com.project.reach.domain.contracts.IMessageRepository
import com.project.reach.domain.contracts.INetworkRepository
import com.project.reach.network.model.DeviceInfo
import com.project.reach.util.debug
import com.project.reach.util.toUUID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactViewModel @Inject constructor(
    private val contactRepository: IContactRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(ContactState())
    val uiState: StateFlow<ContactState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            contactRepository.getSavedContacts().collect { contacts ->
                _uiState.update {
                    it.copy(
                        contacts.map { contact ->
                            Contact(
                                userId = contact.userId,
                                nickname = contact.nickname ?: contact.username
                            )
                        }
                    )
                }
            }
        }
    }

    fun isSaved(userId: String, cb: (Boolean) -> Unit) {
        viewModelScope.launch {
            val saved = contactRepository.isContactSaved(userId.toUUID())
            cb(saved)
        }
    }
}

