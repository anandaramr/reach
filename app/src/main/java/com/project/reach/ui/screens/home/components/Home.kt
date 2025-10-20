package com.project.reach.ui.screens.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.project.reach.ui.screens.home.ChatItem
import com.project.reach.ui.screens.home.TopBar

@Composable
fun Home(
    username: String,
    chatList: List<ChatItem>,
    navigateToChat: () -> Unit
) {
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
                items(chatList) { user ->
                    ChatPreview(
                        navigateToChat,
                        user,
                    )
                }
            }
//
        }
    }
}