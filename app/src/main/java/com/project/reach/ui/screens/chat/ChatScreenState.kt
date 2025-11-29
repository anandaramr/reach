
package com.project.reach.ui.screens.chat

import android.net.Uri

data class ChatScreenState(
    var messageText: String = "",
    var peerName : String = "User",
    var peerId : String = "",
    var isTyping : Boolean = false,
    var fileUri : Uri? = null,
    var fileName: String = ""
)

