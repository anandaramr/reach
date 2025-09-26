package com.project.reach.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.project.reach.ui.screens.home.HomeScreen
import com.project.reach.ui.screens.home.HomeScreenDestination

@Composable
fun AppNavigationHost(
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = rememberNavController(),
        startDestination = HomeScreenDestination.route,
        modifier = modifier
    ) {
        composable (route = HomeScreenDestination.route) {
            HomeScreen()
        }
    }
}