package com.project.reach.ui.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.project.reach.ui.navigation.AppNavigationHost
import com.project.reach.ui.theme.REACHTheme

@Composable
fun ReachApp(
    isOnboardingRequired: Boolean,
    startService: () -> Unit
) {
    REACHTheme {
        Surface(
            modifier = Modifier.Companion.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AppNavigationHost(
                isOnboardingRequired = isOnboardingRequired,
                startService = startService
            )
        }
    }
}
