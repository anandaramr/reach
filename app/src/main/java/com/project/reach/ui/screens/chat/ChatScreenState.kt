
package com.project.reach.ui.screens.chat

import android.net.Uri
import com.project.reach.data.utils.PrivateFile
import com.project.reach.domain.models.TransferState

data class ChatScreenState(
    val messageText: String = "",
    val peerName : String = "User",
    val peerId : String = "",
    val username: String = "",
    val isTyping : Boolean = false,
    val imageUri : Uri? = null,
    val fileName: String = "",
    val fileCaption: String = "",
    val file: PrivateFile? = null,
    val fileUri : Uri? = null,
    val imageName: String = "",
    val imageCaption: String = "",
    val deleteOption: String? = null
)

