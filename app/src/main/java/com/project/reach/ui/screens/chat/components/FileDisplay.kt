package com.project.reach.ui.screens.chat.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.reach.domain.models.Message
import com.project.reach.domain.models.MessageState
import com.project.reach.domain.models.TransferState
import com.project.reach.util.debug
import kotlinx.coroutines.flow.StateFlow
@Composable
fun FileDisplay(
    message: Message.FileMessage,
    getFileUri: (String) -> Uri,
    getTransferState: (String, MessageState) -> StateFlow<TransferState>,
    deleteMessage: (String) -> Unit,
    deleteOption: String?,
    showDeleteOption: (String?) -> Unit
) {
    val fileTransferState by getTransferState(message.fileHash, message.messageState).collectAsState()
    Row(
        modifier = Modifier.fillMaxWidth()
            .combinedClickable(
                onLongClick = { showDeleteOption(message.messageId) },
                onLongClickLabel = "Delete Message",
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    if (deleteOption != null)
                        showDeleteOption(null)
                }
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = if (message.isFromSelf) Alignment.End
            else Alignment.Start,

        ) {
            if (deleteOption == message.messageId)
                Popup(
                    alignment = Alignment.TopCenter,
                    onDismissRequest = { showDeleteOption(null) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .background(MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text("Delete for me",
                            modifier = Modifier
                                .clickable(
                                    onClick = { deleteMessage(message.messageId) },
                                    onClickLabel = "Delete for me"
                                )
                                .padding(5.dp, 10.dp))
                        Text("Delete for everyone",
                            modifier = Modifier.padding(5.dp,10.dp))
                        Text("Copy",
                            modifier = Modifier.padding(5.dp, 10.dp))
                    }
                }
            val context = LocalContext.current
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .clickable(
                        onClick = {
                            openFile(context, uri = getFileUri(message.relativePath), message)
                        }
                    )
                ,
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
                                imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
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
                            val progress = (fileTransferState.currentBytes.toFloat() / message.size.toFloat())
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
                ),
                showDeleteOption = showDeleteOption,
                deleteOption = deleteOption
            )
        }
    }
}
fun openFile(context: Context, uri: Uri, message: Message.FileMessage){
    val mimeType = message.mimeType

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooser = Intent.createChooser(intent, "Open with")

    context.startActivity(chooser)
}