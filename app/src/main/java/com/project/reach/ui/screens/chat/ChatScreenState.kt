
package com.project.reach.ui.screens.chat

data class ChatScreenState(
    var messageText: String = "",
    var peerName : String = "User",
    var peerId : String = "",
    var isTyping : Boolean = false
)

