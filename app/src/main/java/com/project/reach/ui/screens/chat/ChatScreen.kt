package com.project.reach.ui.screens.chat

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.project.reach.ui.navigation.NavigationDestination
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.WavingHand
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.project.reach.domain.models.Message
import com.project.reach.ui.screens.chat.components.FileDisplay
import com.project.reach.ui.screens.chat.components.MessageDisplay
import com.project.reach.ui.screens.chat.components.MessageTextField
import com.project.reach.ui.screens.chat.components.TypingBubble


object ChatScreenDestination : NavigationDestination {
    override val route: String = "chat/{peerId}"
    fun createRoute(peerId: String) = "chat/$peerId"
}


@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatScreenViewModel = hiltViewModel(),
    navigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberLazyListState()
    val messages = viewModel.message.collectAsLazyPagingItems();

    LaunchedEffect(Unit) {
        viewModel.initializeChat()
    }
    Scaffold(
        modifier = modifier.imePadding(), topBar = {
            Column {
                CenterAlignedTopAppBar(
                    modifier = Modifier.height(80.dp),
                    title = {
                        Text(
                            text = uiState.peerName,
                        )
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
        }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp)
                .padding(innerPadding)
                .imePadding()
                .fillMaxSize()
                .clickable(
                    interactionSource = remember() { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        if (uiState.deleteOption != null)
                            viewModel.showDeleteOption(null)
                    },
                ),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            LazyColumn(
                reverseLayout = true,
                modifier = Modifier
                    .weight(1f),
            ) {
                item { Spacer(modifier = Modifier.size(10.dp)) }
                item {
                    if (uiState.isTyping) {
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
                items(count = messages.itemCount) { idx ->
                    messages[idx]?.let { message ->
                        when(message) {
                            is Message.FileMessage -> {
                                FileDisplay(message = message, getFileUri = viewModel::getFileUri, getTransferState = viewModel::getTransferState, deleteMessage = viewModel::deleteMessage, deleteOption = uiState.deleteOption, showDeleteOption = viewModel::showDeleteOption)
                            }
                            is Message.TextMessage -> MessageDisplay(message = message, viewModel::deleteMessage, deleteOption = uiState.deleteOption, showDeleteOption = viewModel::showDeleteOption)
                        }
                    }
                }

                item { Spacer(modifier = Modifier.size(30.dp)) }
            }
            if(messages.itemCount == 0){
                Column (
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
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
                        text = "Say hi to ${uiState.peerName}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            MessageTextField(
                messageText = uiState.messageText,
                fileUri = uiState.fileUri,
                fileName = uiState.fileName,
                imageUri = uiState.imageUri,
                imageCaption = uiState.imageCaption,
                fileCaption = uiState.fileCaption,
                imageName = uiState.imageName,
                isSentEnabled = uiState.file!=null,
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

