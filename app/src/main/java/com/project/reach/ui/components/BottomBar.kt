package com.project.reach.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.project.reach.ui.components.BottomNavBarItem

@Composable
fun BottomBar(navController: NavHostController, items: List<BottomNavBarItem>) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background
    ) {
        items.forEach { it ->
            NavigationBarItem(
                icon = { Icon(it.icon, contentDescription = it.label) },
                selected = currentScreen == it.route,
                label = { Text(it.label) },
                onClick = {
                    navController.navigate(it.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = false
                        }
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