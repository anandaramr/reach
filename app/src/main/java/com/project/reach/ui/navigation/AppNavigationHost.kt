package com.project.reach.ui.navigation
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.project.reach.ui.components.BottomNavBarItem
import com.project.reach.ui.screens.chat.ChatScreen
import com.project.reach.ui.screens.chat.ChatScreenDestination
import com.project.reach.ui.screens.home.HomeScreen
import com.project.reach.ui.screens.home.HomeScreenDestination
import com.project.reach.ui.screens.discover.DiscoverScreenDestination
import com.project.reach.ui.screens.discover.DiscoveryScreen
import com.project.reach.ui.screens.settings.SettingsScreen
import com.project.reach.ui.screens.settings.SettingsScreenDestination

@Composable
fun AppNavigationHost(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = navBackStackEntry?.destination?.route
    Scaffold (
        modifier = Modifier,
        bottomBar = { if(currentScreen != ChatScreenDestination.route) BottomBar(navController) },
    ){ innerPadding ->
        Box(
            modifier = if (currentScreen != ChatScreenDestination.route) {
                Modifier.padding(innerPadding)
            } else {
                Modifier.fillMaxSize()
            }
        ) {
            NavHost(
                navController = navController,
                startDestination = HomeScreenDestination.route,
            ) {
                composable(route = DiscoverScreenDestination.route) {
                    DiscoveryScreen(
                        navigateToHome = { navController.navigate(route = HomeScreenDestination.route) }
                    )
                }
                composable(route = SettingsScreenDestination.route) {
                    SettingsScreen(
                        navigateToHome = { navController.navigate(route = HomeScreenDestination.route) }
                    )
                }
                composable(route = HomeScreenDestination.route) {
                    HomeScreen(
                        navigateToChat = { navController.navigate(route = ChatScreenDestination.route) },
                        navigateToDiscovery = { navController.navigate(route = DiscoverScreenDestination.route) },
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
}
@Composable
fun BottomBar(navController: NavHostController)
{
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = navBackStackEntry?.destination?.route

    val items = listOf(
        BottomNavBarItem("Home", Icons.Default.Home, "home"),
        BottomNavBarItem("Settings", Icons.Default.Settings, "settings"),
        BottomNavBarItem("Discover", Icons.Default.Search, "discover"),
    )
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        items.forEach { it ->
            NavigationBarItem(
                icon = { Icon(it.icon, contentDescription = it.label,) },
                selected = currentScreen == it.route,
                label = { Text(it.label) },
                onClick = {
                    navController.navigate(it.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                    disabledIconColor = MaterialTheme.colorScheme.primary,
                )
            )
        }
    }
}