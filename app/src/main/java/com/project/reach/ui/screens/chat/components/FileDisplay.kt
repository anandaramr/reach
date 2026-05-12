package com.project.reach.ui.screens.chat.components

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.reach.domain.models.Message
import com.project.reach.domain.models.MessageState
import com.project.reach.domain.models.TransferState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
fun FileDisplay(
    message: Message.FileMessage,
    getFileUri: (String) -> Uri,
    getTransferState: (String, MessageState) -> StateFlow<TransferState>,
    deleteMessage: (String) -> Unit,
    selectedMessage: Message?,
    showMessageActions: (Message) -> Unit,
    hideMessageActions: () -> Unit,
    downloadFile: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    val fileTransferState by getTransferState(
        message.fileHash,
        message.messageState
    ).collectAsState()

    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboard.current

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        label = ""
    )

    Row(
        modifier = Modifier
            .combinedClickable(
                onLongClick = { showMessageActions(message) },
                onLongClickLabel = "Delete Message",
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
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment =
                if (message.isFromSelf) Alignment.End
                else Alignment.Start
        ) {
            val context = LocalContext.current
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .clickable(
                        onClick = {
                            openFile(context, uri = getFileUri(message.relativePath), message)
                        }
                    ),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(
                    2.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.outlineVariant,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
            ) {
                Row(
                    modifier = Modifier.padding(15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val fileTransferState = fileTransferState
                    when (fileTransferState) {
                        TransferState.Complete -> {
                            Icon(
                                imageVector = getFileIcon(message.mimeType),
                                contentDescription = "file",
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        TransferState.Paused -> {
                            Icon(
                                imageVector = Icons.Filled.Pause,
                                contentDescription = "file",
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        TransferState.Preparing -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(15.dp),
                                strokeWidth = 2.dp
                            )
                        }

                        is TransferState.Progress -> {
                            val progress =
                                (fileTransferState.currentBytes.toFloat() / message.size.toFloat())
                            CircularProgressIndicator(
                                modifier = Modifier.size(15.dp),
                                progress = { progress },
                                strokeWidth = 2.dp
                            )
                        }
                    }
                    Text(
                        text = message.filename,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .padding(5.dp),
                    )
                }
            }
            ChatBubble(
                message = Message.TextMessage(
                    isFromSelf = message.isFromSelf,
                    text = message.text,
                    messageState = message.messageState,
                    timeStamp = message.timeStamp,
                    messageId = message.messageId,
                )
            )
        }
    }

    if (selectedMessage == message) {
        ModalBottomSheet(
            onDismissRequest = hideMessageActions,
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 10.dp, horizontal = 30.dp)
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
                if (message.text.isNotBlank()) {
                    Text(
                        "Copy",
                        modifier = Modifier
                            .clickable(
                                onClick = {
                                    val clipData =
                                        ClipData.newPlainText("message", message.text)
                                    scope.launch {
                                        clipboard.setClipEntry(clipData.toClipEntry())
                                    }
                                    hideMessageActions()
                                },
                                onClickLabel = "Copy message"
                            )
                            .fillMaxWidth()
                            .padding(5.dp, 15.dp),
                        fontSize = 16.sp,
                    )
                }
                Text(
                    "Download",
                    modifier = Modifier
                        .clickable(
                            onClick = {
                                downloadFile()
                                hideMessageActions()
                            },
                            onClickLabel = "Download file"
                        )
                        .fillMaxWidth()
                        .padding(5.dp, 15.dp),
                    fontSize = 16.sp
                )
            }
        }
    }
}

fun openFile(context: Context, uri: Uri, message: Message.FileMessage) {
    val mimeType = message.mimeType

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooser = Intent.createChooser(intent, "Open with")

    context.startActivity(chooser)
}

fun getFileIcon(mimeType: String): ImageVector {
    return when {
        mimeType.startsWith("image/") -> Icons.Filled.Image
        mimeType.startsWith("video/") -> Icons.Filled.Videocam
        mimeType.startsWith("audio/") -> Icons.Filled.Audiotrack
        mimeType == "application/pdf" -> Icons.Filled.PictureAsPdf

        mimeType in listOf(
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        ) -> Icons.Filled.Description

        mimeType in listOf(
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        ) -> Icons.Filled.TableChart

        mimeType in listOf(
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        ) -> Icons.Filled.Slideshow

        mimeType in listOf(
            "application/zip",
            "application/x-rar-compressed",
            "application/x-7z-compressed"
        ) -> Icons.Filled.FolderZip

        mimeType.startsWith("text/") ->
            Icons.AutoMirrored.Filled.TextSnippet

        mimeType == "application/vnd.android.package-archive" ->
            Icons.Filled.Android

        mimeType in listOf(
            "application/json",
            "application/xml"
        ) -> Icons.Filled.Code

        else -> Icons.AutoMirrored.Filled.InsertDriveFile
    }
}