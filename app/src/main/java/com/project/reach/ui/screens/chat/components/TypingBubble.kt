package com.project.reach.ui.screens.chat.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp

@Composable
fun TypingBubble() {
    val infiniteTransition = rememberInfiniteTransition()
    val dotAnimation = List(3) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 900
                    1f at index * 300
                    0.2f at (index + 1) * 300
                },
            ),
            label = ""
        )
    }

    Row(
        modifier = Modifier.padding(13.dp)
    ){
        dotAnimation.forEach { animatedValue ->
            Card(
                modifier = Modifier.padding(3.dp),
                shape = RoundedCornerShape(100),
            ){
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .alpha(animatedValue.value)
                        .background(MaterialTheme.colorScheme.onSecondary),
                )
            }
        }
    }
}