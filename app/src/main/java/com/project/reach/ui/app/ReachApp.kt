package com.project.reach.ui.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.project.reach.ui.navigation.AppNavigationHost
import com.project.reach.ui.theme.REACHTheme
import com.project.reach.ui.theme.grad

@Composable
fun ReachApp() {
    REACHTheme {
        Surface(
            modifier = Modifier.Companion.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ){
                AppNavigationHost()
            }
        }
    }
}
