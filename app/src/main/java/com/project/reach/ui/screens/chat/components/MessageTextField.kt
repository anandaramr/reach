package com.project.reach.ui.screens.chat.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
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
fun MessageTextField (
    modifier: Modifier = Modifier,
    messageText: String,
    onInputChange: (String) -> Unit,
    sendMessage: (String) -> Unit,
    ) {
    Surface (
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
                IconButton(
                    onClick = { sendMessage(messageText) },
                    enabled = messageText.isNotBlank(),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                    )
                }
            },
            leadingIcon = {
                IconButton(
                    onClick = {}
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add",
                    )
                }
            }
        )
    }
}