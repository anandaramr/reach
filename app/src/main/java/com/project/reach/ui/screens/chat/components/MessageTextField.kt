package com.project.reach.ui.screens.chat.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MessageTextField(
    modifier: Modifier = Modifier,
    messageText: String,
    onInputChange: (String) -> Unit,
    sendMessage: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.padding(),
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
                        onClick = { sendMessage(messageText) },
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
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add",
                        modifier = Modifier.padding(start = 10.dp, end = 5.dp)
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
}