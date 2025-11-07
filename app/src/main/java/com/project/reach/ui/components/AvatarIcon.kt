package com.project.reach.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AvatarIcon(letter: Char, size: AvatarIconSize) {
    val avatarSize =
        when(size) {
            AvatarIconSize.LARGE -> 65
            AvatarIconSize.SMALL -> 20
        }
    Box(
        modifier = Modifier
            .size((avatarSize*2).dp)
            .background(MaterialTheme.colorScheme.outline, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter.toString().uppercase(),
            fontSize = avatarSize.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

enum class AvatarIconSize {
    LARGE,
    SMALL
}
