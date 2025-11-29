package com.project.reach.ui.screens.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.project.reach.ui.screens.chat.components.ChatBubble
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
                .fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween
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
                items(messages.itemCount) { idx ->
                    messages[idx]?.let {message ->
                        ChatBubble(message = message)
                    }
                }
                item { Spacer(modifier = Modifier.size(30.dp)) }
            }
            MessageTextField(
                messageText = uiState.messageText,
                fileUri = uiState.fileUri,
                onInputChange = viewModel::onInputChange,
                sendMessage = viewModel::sendMessage,
                sendFile = viewModel::sendFile,
                changeFileUri = viewModel::changeFileUri,
                changeFileName = viewModel::changeFileName,
                fileName = uiState.fileName
            )
        }
    }
}

