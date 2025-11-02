package com.project.reach.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    startService: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val preview = viewModel.messagePreview.collectAsLazyPagingItems()
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
                .padding(horizontal = 20.dp)
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
                            navigateToChat = {peerId -> navigateToChat(peerId)},
                            username =  preview.username,
                            userId =  preview.userId.toString(),
                            lastMessage = preview.lastMessage,
                        )
                    }
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