package com.project.reach.ui.screens.home

data class HomeScreenState(
    val username: String = "",
    val userId: String = "",
    val connectionMode: ConnectionMode = ConnectionMode.WIFI,
    val needsOnboarding: Boolean = false
)
