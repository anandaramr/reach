package com.project.reach.ui.screens.chat.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.reach.ui.screens.chat.Message

@Composable
fun ChatBubble(
    modifier: Modifier = Modifier,
    message: Message
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement =
            if (message.isFromSelf)
                Arrangement.End
            else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(30.dp),
            border = BorderStroke(
                2.dp,
                if (message.isFromSelf) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier
                .padding(vertical = 4.dp)
                .widthIn(max = 270.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent,
                contentColor =
                    if (message.isFromSelf) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
            ),
        ) {
            Text(
                text = message.text,
                modifier = Modifier
                    .padding(15.dp, 8.dp),
                fontSize = 17.sp
            )
        }
    }
}