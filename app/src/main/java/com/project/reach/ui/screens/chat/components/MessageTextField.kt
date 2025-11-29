package com.project.reach.ui.screens.chat.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun MessageTextField(
    modifier: Modifier = Modifier,
    messageText: String,
    fileUri: Uri?,
    onInputChange: (String) -> Unit,
    sendMessage: (String) -> Unit,
    changeFileUri: (Uri?) -> Unit,
    sendFile: (Uri?) -> Unit,
) {
    Surface(
        modifier = Modifier.padding(),
    ) {
        if (fileUri == null)
            MessageField(
                messageText,
                onInputChange,
                modifier,
                sendMessage,
                changeFileUri,
            )
        else
            FilePreview(
                sendFile,
                fileUri
            )
    }
}

fun getFileName(context: Context, uri: Uri): String {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            return it.getString(nameIndex)
        }
    }
    return "unknown"
}

@Composable
fun FilePreview(
    sendFile: (Uri?) -> Unit,
    fileUri: Uri?
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        fileUri?.let { safeUri ->
            Icon(
                imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                contentDescription = "file",
            )
            val fileName = getFileName(context, fileUri)
            LazyColumn(
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                item {
                    Text(
                        text = fileName,
                        fontSize = 13.sp
                    )
                }
            }
            IconButton(
                onClick = { sendFile(fileUri) },
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                )
            }
        }

    }
}

@Composable
private fun MessageField(
    messageText: String,
    onInputChange: (String) -> Unit,
    modifier: Modifier,
    sendMessage: (String) -> Unit,
    changeFileUri: (Uri?) -> Unit,
) {
    OutlinedTextField(
        value = messageText,
        onValueChange = onInputChange,
        placeholder = { Text("Enter the message") },
        shape = RoundedCornerShape(50.dp),
        modifier = modifier
            .fillMaxWidth(),
        trailingIcon = {
            if (messageText.isNotBlank())
                IconButton(
                    onClick = { sendMessage(messageText) },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                    )
                }
            else
                IconButton(
                    onClick = {
                    },
                ) {
                    Icon(
                        modifier = Modifier.padding(end = 10.dp),
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Record",
                    )
                }
        },
        leadingIcon = {
            Row {
                val pdfPickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocument()
                ) {
                    changeFileUri(it)
                }
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add",
                    modifier = Modifier
                        .padding(start = 10.dp, end = 5.dp)
                        .clickable(
                            onClick = {
                                pdfPickerLauncher.launch(arrayOf("application/pdf"))
                            }
                        )
                )
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Image",
                    modifier = Modifier.padding(end = 10.dp)
                )
            }
        }
    )
}