package com.project.reach.ui.screens.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import com.project.reach.ui.navigation.NavigationDestination
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel


object DiscoverScreenDestination : NavigationDestination {
    override val route: String = "discover"
}

@Composable
fun DiscoveryScreen(
    viewModel: DiscoverViewModel = hiltViewModel(),
    navigateToChat : (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopBar() }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(10.dp)
                .padding()
                .fillMaxSize()
        ) {
            items(items = uiState.peerList) { user ->
                OnlineUser(user, navigateToChat = {peerId -> navigateToChat(peerId)}, saveContact = {peer-> viewModel.saveContact(peer) })
            }
        }
        if(uiState.peerList.isEmpty()){
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
                        .size(120.dp)
                        .padding(20.dp),

                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SearchOff,
                        contentDescription = null,
                        modifier = Modifier
                            .size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.size(25.dp))
                Text(
                    text = "No devices nearby",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    fontSize = 25.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Make sure nearby devices are discoverable.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
@Composable
fun TopBar() {
    CenterAlignedTopAppBar(
        modifier = Modifier.padding(horizontal = 20.dp),
        title = { Text(text = "Nearby devices") }
    )
}