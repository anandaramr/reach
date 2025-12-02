package com.project.reach.ui.screens.chat.components

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.project.reach.util.truncate

@Composable
fun MediaPreview(
    changeName: (String) -> Unit,
    send: (Uri?) -> Unit,
    name: String,
    uri: Uri?,
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        uri?.let { safeUri ->
            Icon(
                imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                contentDescription = "file",
            )
            getName(context, uri, changeName )
            LazyColumn(
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                item {
                    Text(
                        text = name.truncate(60),
                        fontSize = 13.sp
                    )
                }
            }
            IconButton(
                onClick = { send(uri) },
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                )
            }
        }

    }
}
fun getName(context: Context, uri: Uri, changeName:(String)-> Unit) {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            changeName(it.getString(nameIndex))
        }
    }
}