package com.project.reach.ui.screens.discover

import com.project.reach.ui.navigation.NavigationDestination
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel


object DiscoverScreenDestination : NavigationDestination {
    override val route: String = "discover"
}

@Composable
fun DiscoveryScreen(
    viewModel: DiscoverViewModel = viewModel<DiscoverViewModel>(),
) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = { TopBar() },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Text(
            modifier = Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            text = "Devices near you",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.outline
        )
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(10.dp)
                .padding()
                .fillMaxSize()
        ) {
            items(items = uiState.peerNameList) { user ->
                OnlineUser(user)
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