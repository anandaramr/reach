package com.project.reach.ui.app

import androidx.compose.runtime.Composable
import com.project.reach.ui.navigation.AppNavigationHost
import com.project.reach.ui.theme.REACHTheme

@Composable
fun ReachApp() {
    REACHTheme {
        AppNavigationHost()
    }
}
