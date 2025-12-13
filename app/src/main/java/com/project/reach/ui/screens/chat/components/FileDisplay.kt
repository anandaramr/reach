package com.project.reach.ui.screens.chat.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.reach.domain.models.Message
import com.project.reach.domain.models.MessageState

@Composable
fun FileDisplay(filename:String, caption:String){
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (true) Arrangement.End
        else Arrangement.Start
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End,
        ) {
            Card(
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(
                    2.dp,
                    color = MaterialTheme.colorScheme.onSecondary
                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.outlineVariant,
                ),
            ) {
                Row (
                    modifier = Modifier.padding(15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                        contentDescription = "file",
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = filename,
                        fontSize = 15.sp,
                        modifier = Modifier
                            .padding(5.dp),
                    )
                }
            }
            ChatBubble(
                message = Message(
                    isFromSelf = true,
                    text = caption,
                    userId = "123",
                    messageState = MessageState.SENT,
                    timeStamp = 111111111,
                )
            )
        }
    }
}