package com.project.reach.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.project.reach.ui.screens.chat.ChatScreen
import com.project.reach.ui.screens.chat.ChatScreenDestination
import com.project.reach.ui.screens.home.HomeScreen
import com.project.reach.ui.screens.home.HomeScreenDestination
import com.project.reach.ui.screens.discover.DiscoverScreenDestination
import com.project.reach.ui.screens.discover.DiscoveryScreen

@Composable
fun AppNavigationHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = HomeScreenDestination.route,
    ) {
        composable (route = DiscoverScreenDestination.route) {
            DiscoveryScreen(
                navigateToHome = { navController.navigate(route = HomeScreenDestination.route)  }
            )
        }
        composable (route = HomeScreenDestination.route) {
            HomeScreen(
                navigateToChat = { navController.navigate(route = ChatScreenDestination.route)},
                navigateToDiscovery = { navController.navigate(route = DiscoverScreenDestination.route)},
                navigateToHome = { navController.navigate(route = HomeScreenDestination.route) }
            )
        }
        composable (route = ChatScreenDestination.route) {
            ChatScreen (
                navigateBack = { navController.popBackStack( )}
            )
        }
    }
}