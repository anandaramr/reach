package com.project.reach.ui.screens.chat.components

import android.util.Patterns
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.reach.domain.models.Message

@Composable
fun ChatBubble(
    message: Message
) {

    if (message.text != "") {
        Row(
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
                    .padding(vertical = 2.dp)
                    .widthIn(max = 275.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent,
                    contentColor =
                        if (message.isFromSelf) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.background
                ),
            ) {
                ChatMessageText(
                    message = message,
                    modifier = Modifier.padding(18.dp, 10.dp),
                )
            }
        }
    }
}

@Composable
fun ChatMessageText(
    message: Message,
    modifier: Modifier = Modifier,
) {
    val annotatedString = buildAnnotatedString {
        val matcher = Patterns.WEB_URL.matcher(message.text)
        var lastEnd = 0

        while (matcher.find()) {
            append(message.text.substring(lastEnd, matcher.start()))
            val url = matcher.group()
            pushLink(
                LinkAnnotation.Url(
                    url = if (url.startsWith("http")) url else "https://$url",
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = TextDecoration.Underline
                        )
                    )
                )
            )
            append(url)
            pop()
            lastEnd = matcher.end()
        }
        append(message.text.substring(lastEnd))
    }

    Text(
        text = annotatedString,
        modifier = modifier,
        fontSize = 14.sp,
        color = if (message.isFromSelf) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onBackground
    )
}