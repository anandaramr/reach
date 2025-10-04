package com.project.reach.ui.screens.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.reach.ui.components.BottomBar
import com.project.reach.ui.screens.home.HomeScreenState
import com.project.reach.ui.screens.home.HomeScreenViewModel
import com.project.reach.ui.screens.home.TopBar

@Composable
fun Home(
    username: String,
    navigateToDiscovery: () -> Unit,
    uiState: HomeScreenState,
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
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                space = 40.dp,
                alignment = Alignment.CenterVertically
            ),
        ) {
            Text(
                text = "Hi $username",
                fontSize = 40.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(10.dp),
            )

            Text(
                text = uiState.connectionMode.name,
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(10.dp),
            )

            Button(
                onClick = { navigateToChat() }
            ) {
                Text(text = "Begin", fontSize = 15.sp)
            }
        }
    }
}