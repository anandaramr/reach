package com.project.reach.ui.screens.chat.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.project.reach.domain.models.Message

@Composable
fun ChatBubble(
    modifier: Modifier = Modifier,
    message: Message,
    showDeleteOption: (String?) -> Unit,
    deleteOption: String?
) {
    if (message.text != "") {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = if (message.isFromSelf) Arrangement.End
            else Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                shape = RoundedCornerShape(30.dp),
                border = BorderStroke(
                    2.dp,
                    if (message.isFromSelf) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier
                    .padding(vertical = 6.dp)
                    .widthIn(max = 275.dp)
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
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent,
                    contentColor = if (message.isFromSelf) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.background
                ),
            ) {

                Text(
                    text = message.text,
                    modifier = Modifier.padding(18.dp, 10.dp),
                    fontSize = 14.sp,
                    color = if (message.isFromSelf) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}