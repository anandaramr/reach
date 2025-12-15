package com.project.reach.ui.screens.chat.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MessageTextField(
    modifier: Modifier = Modifier,
    messageText: String,
    fileUri: Uri?,
    fileName: String,
    imageUri: Uri?,
    imageName: String,
    fileCaption: String,
    imageCaption: String,
    isSentEnabled: Boolean,
    onInputChange: (String) -> Unit,
    sendMessage: (String) -> Unit,
    sendFile: (String) -> Unit,
    changeFileUri: (Uri?) -> Unit,
    changeFileName: (String) -> Unit,
    changeImageUri: (Uri?) -> Unit,
    changeImageName: (String) -> Unit,
    onFileInputChange: (String) -> Unit,
    onImageInputChange: (String) -> Unit,
    onMediaSelected: (Uri) -> Unit,
) {
    Surface(
        modifier = Modifier.padding(),
    ) {
        if (fileUri != null)
            MediaPreview(
                cancel = changeFileUri,
                changeName = changeFileName,
                send = sendFile,
                uri = fileUri,
                name = fileName,
                caption = fileCaption,
                onCaptionChange = onFileInputChange,
                onMediaSelected = onMediaSelected,
                isSentEnabled = isSentEnabled,
            )
        else if (imageUri != null) {
            MediaPreview(
                cancel = changeImageUri,
                changeName = changeImageName,
                send = sendFile,
                uri = imageUri,
                name = imageName,
                caption = imageCaption,
                onCaptionChange = onImageInputChange,
                onMediaSelected = onMediaSelected,
                isSentEnabled = isSentEnabled,
            )
        } else
            MessageField(
                messageText,
                onInputChange,
                modifier,
                sendMessage,
                changeFileUri,
                changeImageUri,
            )
    }
}


@Composable
private fun MessageField(
    messageText: String,
    onInputChange: (String) -> Unit,
    modifier: Modifier,
    sendMessage: (String) -> Unit,
    changeFileUri: (Uri?) -> Unit,
    changeImageUri: (Uri?) -> Unit,
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
        },
        leadingIcon = {
            Row {
                val pdfPickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocument()
                ) {
                    changeFileUri(it)
                }
                val imagePickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickVisualMedia()
                ) {
                    changeImageUri(it)
                }
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add",
                    modifier = Modifier
                        .padding(start = 10.dp, end = 5.dp)
                        .clickable(
                            onClick = {
                                pdfPickerLauncher.launch(arrayOf("*/*"))
                            }
                        )
                )
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Image",
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .clickable(
                            onClick = {
                                imagePickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                )
                            }
                        )
                )
            }
        }
    )
}


