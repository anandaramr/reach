package com.project.reach.ui.screens.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.reach.ui.screens.chat.components.ChatBubble
import com.project.reach.ui.screens.chat.components.MessageTextField
import com.project.reach.ui.screens.discover.Peer
import com.project.reach.util.debug
import dagger.hilt.android.lifecycle.HiltViewModel


object ChatScreenDestination : NavigationDestination {
    override val route: String = "chat/{peerId}"
    fun createRoute(peerId: String) = "chat/$peerId"
}


@Composable
fun ChatScreen(
    peerId: String,
    modifier: Modifier = Modifier,
    viewModel: ChatScreenViewModel = hiltViewModel(),
    navigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberLazyListState()
    LaunchedEffect(Unit) {
        viewModel.initializeChat(peerId)
    }
    Scaffold(
        modifier = modifier
            .imePadding(),
        topBar = {
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp)
                .padding(innerPadding)
                .imePadding()
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            LazyColumn(
                reverseLayout = true,
                modifier = Modifier
                    .padding(top = 25.dp)
                    .weight(1f),
            ) {
                items(uiState.messageList.reversed()) { message ->
                    ChatBubble(message = message)
                }
            }
            MessageTextField(
                messageText = uiState.messageText,
                onInputChange = viewModel::onInputChange,
                sendMessage = viewModel::sendMessage,
            )
        }
    }
}

