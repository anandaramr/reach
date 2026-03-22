package com.project.reach.ui.screens.calls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.project.reach.domain.models.CallState
import com.project.reach.ui.components.AvatarIcon
import com.project.reach.ui.components.AvatarIconSize

@Composable
fun CallScreen(
    state: CallState,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onHangUp: () -> Unit,
    onCancel: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val letter: Char = when (state) {
                is CallState.Incoming -> state.nickname?.firstOrNull() ?: 'U'
                is CallState.Outgoing -> state.nickname?.firstOrNull() ?: 'U'
                is CallState.Connected -> state.nickname?.firstOrNull() ?: 'U'
                is CallState.Disconnected -> state.nickname?.firstOrNull() ?: 'U'
                else -> ' '
            }

            if (letter != ' ')
                AvatarIcon(
                    letter = letter,
                    size = AvatarIconSize.LARGE
                )

            Spacer(modifier = Modifier.height(16.dp))

            if (state is CallState.Disconnected) {
                Text(
                    text = state.reason,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = when (state) {
                    is CallState.Incoming -> "Incoming Call..."
                    is CallState.Outgoing -> "Calling..."
                    is CallState.Connected -> "Ongoing Call"
                    is CallState.Disconnected -> "Call Ended"
                    else -> ""
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = when (state) {
                    is CallState.Incoming -> state.nickname ?: "Unknown"
                    is CallState.Outgoing -> state.nickname ?: "Unknown"
                    is CallState.Connected -> state.nickname ?: "Unknown"
                    else -> ""
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            when (state) {
                is CallState.Incoming -> {
                    FloatingActionButton(onClick = onAccept, containerColor = Color.Green) {
                        Icon(Icons.Default.Call, contentDescription = "Answer")
                    }
                    FloatingActionButton(onClick = onReject, containerColor = Color.Red) {
                        Icon(Icons.Default.CallEnd, contentDescription = "Reject")
                    }
                }

                is CallState.Connected, is CallState.Outgoing -> {
                    FloatingActionButton(onClick = onHangUp, containerColor = Color.Red) {
                        Icon(Icons.Default.CallEnd, contentDescription = "Hang Up")
                    }
                }

                is CallState.Disconnected -> {
                    FloatingActionButton(
                        onClick = onCancel,
                        containerColor = MaterialTheme.colorScheme.secondary
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Exit")
                    }
                    FloatingActionButton(onClick = onAccept, containerColor = Color.Green) {
                        Icon(Icons.Default.Call, contentDescription = "Answer")
                    }
                }

                is CallState.Idle -> {}
            }
        }
    }
}

//@Preview
//@Composable
//fun CallPreview() {
//    CallScreen(
//        state = CallState.Disconnected(
//            callId = UUID.randomUUID(),
//            peerId = UUID.randomUUID(),
//            username = "Devika",
//            nickname = "Devu",
//            reason = "Callee is busy"
//        ),
//        onAccept = {},
//        onReject = {},
//        onHangUp = {},
//        onCancel = {}
//    )
//}
