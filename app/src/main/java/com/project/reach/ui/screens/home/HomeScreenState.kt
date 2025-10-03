package com.project.reach.ui.screens.home

import com.project.reach.ui.screens.chat.UserPreview

data class HomeScreenState(
    val user : UserPreview = UserPreview("User")
)
