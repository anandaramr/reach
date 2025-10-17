package com.project.reach.ui.screens.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.reach.ui.components.BottomBar
import com.project.reach.ui.screens.chat.components.ChatBubble
import com.project.reach.ui.screens.home.ChatItem
import com.project.reach.ui.screens.home.HomeScreenState
import com.project.reach.ui.screens.home.HomeScreenViewModel
import com.project.reach.ui.screens.home.TopBar

@Composable
fun Home(
    username: String,
    chatList: List<ChatItem>,
    navigateToDiscovery: () -> Unit,
    navigateToChat: () -> Unit
) {
    Scaffold(
        topBar = { TopBar() },
        bottomBar = { BottomBar(currentScreen = "home", navigate = navigateToDiscovery) },
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