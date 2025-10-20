package com.project.reach.ui.navigation

import android.annotation.SuppressLint
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.project.reach.ui.components.bottomBar.BottomBar
import com.project.reach.ui.components.bottomBar.BottomNavBarItem
import com.project.reach.ui.screens.chat.ChatScreen
import com.project.reach.ui.screens.chat.ChatScreenDestination
import com.project.reach.ui.screens.home.HomeScreen
import com.project.reach.ui.screens.home.HomeScreenDestination
import com.project.reach.ui.screens.discover.DiscoverScreenDestination
import com.project.reach.ui.screens.discover.DiscoveryScreen
import com.project.reach.ui.screens.settings.SettingsScreen
import com.project.reach.ui.screens.settings.SettingsScreenDestination
import kotlin.collections.contains

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppNavigationHost(
    navController: NavHostController = rememberNavController()
) {
    val items = listOf(
        BottomNavBarItem("Home", Icons.Default.Home, "home"),
        BottomNavBarItem("Discover", Icons.Default.Search, "discover"),
        BottomNavBarItem("Settings", Icons.Default.Settings, "settings")
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = navBackStackEntry?.destination?.route
    val displayBottomBar = currentScreen in items.map { it.route }

    Scaffold(
        modifier = Modifier,
        bottomBar = {
            if (displayBottomBar) {
                BottomBar(navController, items)
            }
        }

    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeScreenDestination.route,
        ) {
            composable(route = DiscoverScreenDestination.route) {
                DiscoveryScreen()
            }
            composable(route = SettingsScreenDestination.route) {
                SettingsScreen()
            }
            composable(route = HomeScreenDestination.route) {
                HomeScreen(
                    navigateToChat = { navController.navigate(route = ChatScreenDestination.route) },
                )
            }
            composable(route = ChatScreenDestination.route) {
                ChatScreen(
                    navigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

