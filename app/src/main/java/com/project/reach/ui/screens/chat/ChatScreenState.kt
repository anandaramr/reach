
package com.project.reach.ui.screens.chat

data class ChatScreenState(
    var messageText: String = "",
    var messageList: List<Message> = emptyList(),
    var peerName : String = "User",
    var peerId : String = ""
)

