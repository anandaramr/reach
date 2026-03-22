package com.project.reach.ui.screens.chat

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.project.reach.domain.contracts.ICallRepository
import com.project.reach.domain.contracts.IContactRepository
import com.project.reach.domain.contracts.IFileRepository
import com.project.reach.domain.contracts.IMessageRepository
import com.project.reach.domain.models.MessageState
import com.project.reach.domain.models.TransferState
import com.project.reach.util.debug
import com.project.reach.util.toUUID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val messageRepository: IMessageRepository,
    private val contactRepository: IContactRepository,
    private val fileRepository: IFileRepository,
    private val callRepository: ICallRepository

): ViewModel() {
    private val _uiState = MutableStateFlow(ChatScreenState())
    val uiState: StateFlow<ChatScreenState> = _uiState.asStateFlow()
    private val peerId: String = savedStateHandle["peerId"] ?: ""

    val message = messageRepository.getMessagesPaged(peerId).cachedIn(viewModelScope)

    private fun updateMessageText(text: String) {
        _uiState.update { currentState ->
            currentState.copy(messageText = text)
        }
    }

    fun onInputChange(text: String) {
        updateMessageText(text)
        messageRepository.emitTyping(_uiState.value.peerId)
    }

    fun startCall(){
        viewModelScope.launch {
            callRepository.startCall(peerId.toUUID())
        }
    }

    fun onFileInputChange(text: String) {
        updateFileCaption(text)
        messageRepository.emitTyping(_uiState.value.peerId)
    }

    private fun updateFileCaption(text: String) {
        _uiState.update {
            it.copy(
                fileCaption = text
            )
        }
    }

    fun showDeleteOption(show: String?) {
        displayDeleteOption(show)
    }

    fun onImageInputChange(text: String) {
        updateImageCaption(text)
        messageRepository.emitTyping(_uiState.value.peerId)
    }

    fun deleteMessage(messageId: String) {
        removeMessage(messageId)
    }

    private fun updateImageCaption(text: String) {
        _uiState.update {
            it.copy(
                imageCaption = text
            )
        }
    }

    private fun displayDeleteOption(show: String?) {
        _uiState.update {
            it.copy(
                deleteOption = show
            )
        }
    }

    private fun removeMessage(messageId: String) {
        viewModelScope.launch {
            messageRepository.deleteMessage(messageId)
        }
    }

    fun sendMessage(text: String) {
        updateMessageText("")
        viewModelScope.launch {
            messageRepository.sendMessage(_uiState.value.peerId, text)
        }
    }

    //  ---------------------------------------------------------------------------------------
    // Public function that can be accessed by UI functions to store File Uri, File name and Image Uri
    fun changeFileUri(uri: Uri?) {
        storeFileUri(uri)
    }

    fun changeImageUri(uri: Uri?) {
        debug(uri.toString())
        storeImageUri(uri)
    }

    fun changeImageName(imageName: String) {
        storeImageName(imageName)
    }

    fun changeFileName(fileName: String) {
        storeFileName(fileName)
    }

    //  ---------------------------------------------------------------------------------------
    // Storing File and Image details in state
    private fun storeFileUri(uri: Uri?) {
        _uiState.update {
            it.copy(
                fileUri = uri
            )
        }
    }

    private fun storeFileName(fileName: String) {
        _uiState.update {
            it.copy(
                fileName = fileName
            )
        }
    }

    private fun storeImageUri(uri: Uri?) {
        _uiState.update {
            it.copy(
                imageUri = uri
            )
        }
    }

    private fun storeImageName(imageName: String) {
        _uiState.update {
            it.copy(
                imageName = imageName
            )
        }
    }
//  ---------------------------------------------------------------------------------------

    fun sendFile(caption: String) {
        onImageInputChange("")
        onFileInputChange("")
        changeImageUri(null)
        changeFileUri(null)
        viewModelScope.launch {
            messageRepository.sendMessage(_uiState.value.peerId, caption, _uiState.value.file)
            _uiState.update{
                it.copy(
                    file = null
                )
            }
        }
    }

    fun onMediaSelected(uri: Uri) {
        viewModelScope.launch {
            val file = fileRepository.saveFileToPrivateStorage(uri, onProgress = {})
            _uiState.update {
                it.copy(
                    file = file
                )
            }
        }
    }

    private suspend fun updateUserState(peerId: String) {
        val user = contactRepository.getContact(peerId).first()
        _uiState.update {
            it.copy(
                peerName = user.nickname ?: user.username,
                username = user.username
            )
        }
    }

    fun getFileUri(relativePath: String): Uri {
        val uri = fileRepository.getContentUri(relativePath)
        return uri
    }

    fun initializeChat() {
        viewModelScope.launch {
            messageRepository.isTyping(peerId).collect { typeStatus ->
                _uiState.update { it.copy(isTyping = typeStatus) }
            }
        }
        _uiState.update { it.copy(peerId = peerId) }
        viewModelScope.launch {
            updateUserState(peerId = peerId)
        }

    }

    fun getTransferState(fileHash: String, messageState: MessageState): StateFlow<TransferState> {
        return fileRepository
            .observeTransferState(fileHash, messageState)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = when (messageState) {
                    MessageState.DELIVERED -> TransferState.Complete
                    MessageState.PAUSED -> TransferState.Paused
                    else -> TransferState.Preparing
                }

            )
    }
}

