package com.project.reach.ui.screens.chat

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.outlined.WavingHand
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.project.reach.domain.models.Message
import com.project.reach.domain.models.MessageState
import com.project.reach.domain.models.TransferState
import com.project.reach.ui.app.LocalPermissionHandler
import com.project.reach.ui.navigation.NavigationDestination
import com.project.reach.ui.screens.chat.components.FileDisplay
import com.project.reach.ui.screens.chat.components.MessageDisplay
import com.project.reach.ui.screens.chat.components.MessageTextField
import com.project.reach.ui.screens.chat.components.TypingBubble
import kotlinx.coroutines.flow.StateFlow
import kotlin.reflect.KFunction1
import kotlin.reflect.KFunction2

object ChatScreenDestination: NavigationDestination {
    override val route: String = "chat/{peerId}"
    val deepLinkPattern: String = "chat/{peerId}"
    fun createDeepLinkUri(peerId: String) = "chat/$peerId"
    fun createRoute(peerId: String) = "chat/$peerId"
}

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatScreenViewModel = hiltViewModel(),
    navigateBack: () -> Unit,
    navigateToViewContact: (String, String, String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberLazyListState()
    val messages = viewModel.message.collectAsLazyPagingItems()
    val permissionHandler = LocalPermissionHandler.current

    LaunchedEffect(Unit) {
        viewModel.initializeChat()
    }
    Scaffold(
        modifier = modifier.imePadding(), topBar = {
            TopBar(
                peerId = uiState.peerId,
                username = uiState.username,
                peerName = uiState.peerName,
                navigateToViewContact = navigateToViewContact,
                requestMicrophonePermission = { onGranted -> permissionHandler.onMicrophonePermissionGranted(onGranted) },
                startCall = viewModel::startCall,
                navigateBack = navigateBack,
            )
        }) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp, end = 20.dp)
                .padding(innerPadding)
                .imePadding()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (uiState.deleteOption != null) {
                        viewModel.showDeleteOption(null)
                    }
                }
        ) {
            if (messages.itemCount == 0 && messages.loadState.refresh == LoadState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            } else {
                if (messages.itemCount == 0) {
                    EmptyChatScreen(uiState.peerName)
                } else {
                    ChatList(
                        scrollState = scrollState,
                        isTyping = uiState.isTyping,
                        messages = messages,
                        getFileUri = viewModel::getFileUri,
                        getTransferState = viewModel::getTransferState,
                        deleteMessage = viewModel::deleteMessage,
                        deleteOption = uiState.deleteOption,
                        showDeleteOption = viewModel::showDeleteOption
                    )
                }
            }
            MessageTextField(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                messageText = uiState.messageText,
                fileUri = uiState.fileUri,
                fileName = uiState.fileName,
                imageUri = uiState.imageUri,
                imageCaption = uiState.imageCaption,
                fileCaption = uiState.fileCaption,
                imageName = uiState.imageName,
                isFileAttached = uiState.file != null,
                sendMessage = viewModel::sendMessage,
                sendFile = viewModel::sendFile,
                onMediaSelected = viewModel::onMediaSelected,
                onInputChange = viewModel::onInputChange,
                changeFileUri = viewModel::changeFileUri,
                changeFileName = viewModel::changeFileName,
                changeImageUri = viewModel::changeImageUri,
                changeImageName = viewModel::changeImageName,
                onFileInputChange = viewModel::onFileInputChange,
                onImageInputChange = viewModel::onImageInputChange,
            )
        }
    }
}

@Composable
private fun TopBar(
    peerId: String,
    username: String,
    peerName: String,
    navigateToViewContact: (String, String, String) -> Unit,
    requestMicrophonePermission: (onGranted: () -> Unit) -> Unit,
    startCall: () -> Unit,
    navigateBack: () -> Unit
) {
    Column {
        CenterAlignedTopAppBar(
            modifier = Modifier.height(80.dp),
            title = {
                Text(
                    text = peerName,
                    modifier = Modifier.clickable(
                        onClick = {
                            navigateToViewContact(
                                peerId,
                                username,
                                peerName
                            )
                        }
                    )
                )
            },
            actions = {
                IconButton(
                    onClick = {
                        requestMicrophonePermission(startCall)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Call,
                        contentDescription = "Call",
                    )
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = navigateBack,
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBackIosNew,
                        contentDescription = "Back",
                    )
                }
            },
        )
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun ChatList(
    scrollState: LazyListState,
    isTyping: Boolean,
    messages: LazyPagingItems<Message>,
    getFileUri: KFunction1<String, Uri>,
    getTransferState: KFunction2<String, MessageState, StateFlow<TransferState>>,
    deleteMessage: KFunction1<String, Unit>,
    deleteOption: String?,
    showDeleteOption: KFunction1<String?, Unit>
) {
    LazyColumn(
        reverseLayout = true,
        state = scrollState,
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 72.dp)
    ) {
        item { Spacer(modifier = Modifier.size(10.dp)) }
        item {
            if (isTyping) {
                Card(
                    shape = RoundedCornerShape(30.dp),
                    border = BorderStroke(
                        2.dp, MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.outline
                    ),
                ) {
                    TypingBubble()
                }
            }
        }
        items(count = messages.itemCount, key = { index ->
            messages[index]?.messageId ?: "placeholder-$index"
        }) { idx ->
            messages[idx]?.let { message ->
                when (message) {
                    is Message.FileMessage -> {
                        FileDisplay(
                            message = message,
                            getFileUri = getFileUri,
                            getTransferState = getTransferState,
                            deleteMessage = deleteMessage,
                            deleteOption = deleteOption,
                            showDeleteOption = showDeleteOption
                        )
                    }

                    is Message.TextMessage -> MessageDisplay(
                        message = message,
                        deleteMessage = deleteMessage,
                        deleteOption = deleteOption,
                        showDeleteOption = showDeleteOption
                    )
                }
            }
        }
        item { Spacer(modifier = Modifier.size(30.dp)) }
    }
}

@Composable
private fun EmptyChatScreen(peerName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.8f),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.outlineVariant,
                    shape = CircleShape
                )
                .size(120.dp)
                .padding(20.dp),

            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.WavingHand,
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
            )
        }

        Spacer(modifier = Modifier.size(25.dp))
        Text(
            text = "Start the conversation",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            fontSize = 25.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Say hi to ${peerName}.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

