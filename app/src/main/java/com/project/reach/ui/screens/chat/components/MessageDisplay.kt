package com.project.reach.ui.screens.chat.components

import android.content.ClipData
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.reach.domain.models.Message
import kotlinx.coroutines.launch

@Composable
fun MessageDisplay(
    message: Message.TextMessage,
    deleteMessage: (String) -> Unit,
    selectedMessage: Message?,
    showMessageActions: (Message) -> Unit,
    hideMessageActions: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboard.current

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        label = ""
    )

    Box(
        modifier = Modifier
            .combinedClickable(
                onLongClick = { showMessageActions(message) },
                onLongClickLabel = "Open message actions menu",
                indication = null,
                interactionSource = interactionSource,
                onClick = {
                    if (selectedMessage != null)
                        hideMessageActions()
                }
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .fillMaxWidth(),
    ) {
        ChatBubble(
            message = message
        )
    }

    if (selectedMessage == message) {
        ModalBottomSheet(
            onDismissRequest = hideMessageActions,
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
                        .padding(5.dp, 15.dp),
                    fontSize = 16.sp,
                    color = Color.Red
                )
                Text(
                    "Copy",
                    modifier = Modifier
                        .clickable(
                            onClick = {
                                val clipData = ClipData.newPlainText("message", message.text)
                                scope.launch {
                                    clipboard.setClipEntry(clipData.toClipEntry())
                                }
                                hideMessageActions()
                            },
                            onClickLabel = "Copy message"
                        )
                        .fillMaxWidth()
                        .padding(5.dp, 15.dp),
                    fontSize = 16.sp
                )
            }
        }
    }
}