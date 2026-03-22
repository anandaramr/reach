package com.project.reach.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.pow

@Composable
fun AvatarIcon(letter: Char, size: AvatarIconSize) {
    val avatarSize =
        when(size) {
            AvatarIconSize.LARGE -> 65
            AvatarIconSize.SMALL -> 27
        }
    Box(
        modifier = Modifier
            .size((avatarSize/0.6).dp)
            .background(Color.Transparent, CircleShape)
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary,
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter.toString().uppercase(),
            fontSize = avatarSize.sp,
            fontFamily = FontFamily.SansSerif,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

enum class AvatarIconSize {
    LARGE,
    SMALL
}
