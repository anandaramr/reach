package com.project.reach.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun BottomBar(
    currentScreen:String,
    navigate: () -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        NavigationBarItem(
            enabled = currentScreen=="discovery",
            icon = { Icon(Icons.Default.Home, contentDescription = "Home",)},
            selected = currentScreen == "home",
            label = null,
            onClick = { navigate() },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent,
                selectedIconColor = MaterialTheme.colorScheme.primary,
            )
        )
        NavigationBarItem(
            enabled = currentScreen=="home",
            icon = { Icon(Icons.Default.Search, contentDescription = "Discover")},
            selected = currentScreen == "discovery",
            label = null,
            onClick = { navigate() },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent,
                selectedIconColor = MaterialTheme.colorScheme.primary,
            )
        )
    }
}