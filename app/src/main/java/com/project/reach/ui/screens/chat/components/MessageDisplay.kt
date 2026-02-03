package com.project.reach.ui.screens.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.zIndex
import com.project.reach.domain.models.Message

@Composable
fun MessageDisplay(message: Message.TextMessage, deleteMessage: (String) -> Unit, deleteOption: String?, showDeleteOption: (String?) -> Unit) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    Box(){
        ChatBubble(message = message, deleteOption = deleteOption, showDeleteOption = showDeleteOption)
        if (deleteOption == message.messageId)
            ModalBottomSheet(
                onDismissRequest = { showDeleteOption(null) },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                ) {
                    Text(
                        "Delete for me",
                        modifier = Modifier
                            .clickable(
                                onClick = { deleteMessage(message.messageId) },
                                onClickLabel = "Delete for me"
                            )
                            .fillMaxWidth()
                            .padding(5.dp, 10.dp),
                        fontSize = 14.sp
                    )
                    Text(
                        "Delete for everyone",
                        modifier = Modifier
                            .padding(5.dp, 10.dp),
                        fontSize = 14.sp
                    )
                    Text(
                        "Copy",
                        modifier = Modifier.padding(5.dp, 10.dp),
                        fontSize = 14.sp
                    )
                }
            }
    }
}