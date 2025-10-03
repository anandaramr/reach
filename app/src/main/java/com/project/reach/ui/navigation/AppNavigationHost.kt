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
import com.project.reach.ui.screens.login.LoginScreen
import com.project.reach.ui.screens.login.LoginScreenDestination

@Composable
fun AppNavigationHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = LoginScreenDestination.route,
    ) {
        composable (route = LoginScreenDestination.route) {
            LoginScreen(
                navigateToHome = { navController.navigate(route = HomeScreenDestination.route)  }
            )
        }
        composable (route = HomeScreenDestination.route) {
            HomeScreen(
                navigateToChat = { navController.navigate(route = ChatScreenDestination.route)}
            )
        }
        composable (route = ChatScreenDestination.route) {
            ChatScreen (
                navigateBack = { navController.popBackStack( )}
            )
        }
    }
}