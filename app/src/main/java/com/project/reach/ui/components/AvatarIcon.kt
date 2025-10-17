package com.project.reach.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AvatarIcon(
    letter: Char
) {
    Card(
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            letter.toString().uppercase(),
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 15.dp),
        )
    }
}