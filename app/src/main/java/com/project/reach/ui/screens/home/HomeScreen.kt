package com.project.reach.ui.screens.home

import android.graphics.drawable.Icon
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.project.reach.ui.navigation.NavigationDestination
import com.project.reach.ui.screens.home.components.ChatPreview

object HomeScreenDestination: NavigationDestination {
    override val route: String
        get() = "home"
}

@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel = hiltViewModel(),
    navigateToChat: (String) -> Unit,
    navigateToDiscover: ()-> Unit,
    startService: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val preview = viewModel.messagePreview.collectAsLazyPagingItems()
    val typingUsers by viewModel.typingUsers.collectAsState()

    LaunchedEffect(Unit) {
        startService()
    }

    Scaffold(
        topBar = { TopBar() },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                space = 40.dp,
                alignment = Alignment.CenterVertically
            ),
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(top = 25.dp)
                    .weight(1f),
            ) {
                items(preview.itemCount) { idx ->
                    preview[idx]?.let { preview ->
                        ChatPreview(
                            navigateToChat = { peerId -> navigateToChat(peerId) },
                            username = preview.username,
                            userId = preview.userId.toString(),
                            lastMessage = preview.lastMessage,
                            isTyping = preview.userId.toString() in typingUsers
                        )
                    }
                }
            }
            if(preview.itemCount == 0){
                Column (
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape
                            )
                            .clickable(onClick = { navigateToDiscover() })
                            .size(120.dp)
                            .padding(20.dp),

                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = null,
                            modifier = Modifier
                                .size(60.dp)
                        )
                    }

                    Spacer(modifier = Modifier.size(25.dp))
                    Text(
                        text = "It’s quiet in here",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        fontSize = 25.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Discover new people to start the conversation.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

    }
}

@Composable
fun TopBar() {
    CenterAlignedTopAppBar(
        modifier = Modifier.padding(horizontal = 20.dp),
        title = { Text(text = "REACH") }
    )
}