
package com.project.reach.ui.screens.chat

import android.net.Uri
import com.project.reach.data.utils.PrivateFile
import com.project.reach.domain.models.TransferState

data class ChatScreenState(
    var messageText: String = "",
    var peerName : String = "User",
    var peerId : String = "",
    var isTyping : Boolean = false,
    var imageUri : Uri? = null,
    var fileName: String = "",
    var fileCaption: String = "",
    var file: PrivateFile? = null,
    var fileUri : Uri? = null,
    var imageName: String = "",
    var imageCaption: String = "",
    val deleteOption: String? = null
)

