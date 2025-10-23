package com.project.reach.ui.screens.home

import com.project.reach.domain.models.MessagePreview

data class HomeScreenState(
    val chatList: List<MessagePreview> = emptyList(),
)
