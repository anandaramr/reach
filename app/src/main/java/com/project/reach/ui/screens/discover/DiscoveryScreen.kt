package com.project.reach.ui.screens.discover

import com.project.reach.ui.navigation.NavigationDestination
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.reach.ui.components.BottomBar


object DiscoverScreenDestination : NavigationDestination {
    override val route: String = "discover"
}

@Composable
fun DiscoveryScreen(
//    viewModel: HomeScreenViewModel = hiltViewModel(),
    navigateToHome: () -> Unit,
) {
    Scaffold(
        bottomBar = { BottomBar(currentScreen = "discovery", navigate = navigateToHome) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                40.dp,
                alignment = Alignment.CenterVertically
            ),
        ){
            Text(
                text = "Discover",
                fontSize = 40.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(10.dp),
            )
//
//            Button(
//                onClick = { navigateToChat() }
//            ) {
//                Text(
//                    text = "Begin",
//                    fontSize = 15.sp,
//                )
//            }
        }
    }
}
