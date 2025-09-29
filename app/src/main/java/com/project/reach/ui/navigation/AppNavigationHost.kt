package com.project.reach.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.project.reach.ui.screens.chat.ChatScreen
import com.project.reach.ui.screens.chat.ChatScreenRoute
import com.project.reach.ui.screens.home.HomeScreen
import com.project.reach.ui.screens.home.HomeScreenDestination

@Composable
fun AppNavigationHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = HomeScreenDestination.route,
    ) {
        composable (route = HomeScreenDestination.route) {
            HomeScreen(
                navigateToChat = { navController.navigate(route = ChatScreenRoute.route)  }
            )
        }
        composable (route = ChatScreenRoute.route) {
            ChatScreen (
                navigateBack = { navController.popBackStack( )}
            )
        }
    }
}