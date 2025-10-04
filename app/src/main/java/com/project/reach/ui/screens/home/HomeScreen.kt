package com.project.reach.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.reach.ui.components.BottomBar
import com.project.reach.ui.components.LoginScreen
import com.project.reach.ui.navigation.NavigationDestination

object HomeScreenDestination: NavigationDestination {
    override val route: String
        get() = "home"
}

@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel = viewModel(),
    navigateToChat: ()-> Unit,
    navigateToHome: ()-> Unit,
    navigateToDiscovery: ()-> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    if(!uiState.userId.isEmpty()) Home(viewModel, navigateToDiscovery, uiState, navigateToChat)
    else LoginScreen(viewModel, navigateToHome = navigateToHome)
}

@Composable
fun Home(viewModel: HomeScreenViewModel, navigateToDiscovery: ()->Unit , uiState: HomeScreenState, navigateToChat: () -> Unit) {
    Scaffold(
        topBar = { com.project.reach.ui.screens.home.TopBar(viewModel) },
        bottomBar = { BottomBar(currentScreen = "home", navigate = navigateToDiscovery) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                40.dp,
                alignment = Alignment.CenterVertically
            ),
        ) {
            Text(
                text = "Hi ${uiState.username}",
                fontSize = 40.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(10.dp),
            )
            Text(
                text = uiState.connectionMode.name,
                fontSize = 20.sp,
//                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(10.dp),
            )
            Button(
                onClick = { navigateToChat() }
            ) {
                Text(
                    text = "Begin",
                    fontSize = 15.sp,
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(viewModel: HomeScreenViewModel) {
    CenterAlignedTopAppBar(
        modifier = Modifier.padding(horizontal = 20.dp),
        title = { Text(text = "REACH") },
        actions = {
            DropDownMenu(viewModel)
        }
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDownMenu(viewModel: HomeScreenViewModel) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(ConnectionMode.WIFI) }
    val connectionMode = ConnectionMode.entries
    val trailingIconList = mapOf(
        ConnectionMode.WIFI to Icons.Default.Wifi,
        ConnectionMode.BLUETOOTH to Icons.Filled.Bluetooth,
        ConnectionMode.WIFI_DIRECT to Icons.Filled.Wifi,
    )
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        Icon(
            imageVector = trailingIconList[selectedOption]!!,
            contentDescription = selectedOption.name,
            modifier = Modifier
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            modifier = Modifier.fillMaxWidth(0.3f),
            onDismissRequest = { expanded = false }
        ) {
            connectionMode.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.name,) },
                    onClick = {
                        viewModel.changeConnectionMode(option)
                        selectedOption = option
                        expanded = false
                    }
                )
            }
        }
    }
}
