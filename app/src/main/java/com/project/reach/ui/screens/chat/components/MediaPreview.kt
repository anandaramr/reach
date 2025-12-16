package com.project.reach.ui.screens.chat.components

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.reach.util.truncate

@Composable
fun MediaPreview(
    cancel: (Uri?) -> Unit,
    changeName: (String) -> Unit,
    send: (String) -> Unit,
    name: String,
    uri: Uri?,
    caption: String,
    onCaptionChange: (String) -> Unit,
    onMediaSelected: (Uri) -> Unit,
    isSentEnabled: Boolean
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
        uri?.let { safeUri ->
            getName(context, uri, changeName)
            onMediaSelected(uri)
            ModalBottomSheet(
                modifier = Modifier
                    .fillMaxHeight(),
                onDismissRequest = { cancel(null) },
                sheetState = sheetState
            ) {
                Column (
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(16.dp)
                        .imePadding(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    if(!isSentEnabled) CircularProgressIndicator()
                    Column(
                        modifier = Modifier
                            .fillMaxHeight(0.7f)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                            contentDescription = "file",
                            modifier = Modifier.size(90.dp)
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                        Text(
                            text = name.truncate(60),
                            fontSize = 13.sp
                        )
                    }
                    OutlinedTextField(
                        value = caption,
                        onValueChange = onCaptionChange,
                        placeholder = { Text("Enter the text") },
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(
                                enabled = isSentEnabled,
                                onClick = { send(caption) }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                )
                            }
                        }
                    )
                }
            }
        }

    }
}

fun getName(context: Context, uri: Uri, changeName: (String) -> Unit) {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            changeName(it.getString(nameIndex))
        }
    }
}