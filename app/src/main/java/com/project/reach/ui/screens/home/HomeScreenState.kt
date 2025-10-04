package com.project.reach.ui.screens.home

import com.project.reach.ui.screens.chat.UserPreview

data class HomeScreenState(
    val username : String = "",
    val userId : String = "",
    val connectionMode: ConnectionMode = ConnectionMode.WIFI,
    )
