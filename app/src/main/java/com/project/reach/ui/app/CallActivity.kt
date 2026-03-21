package com.project.reach.ui.app

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.reach.domain.contracts.ICallRepository
import com.project.reach.domain.models.CallState
import com.project.reach.ui.theme.REACHTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CallActivity: ComponentActivity() {

    @Inject
    lateinit var callRepository: ICallRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configureLockScreenFlags()

        setContent {
            val currentState by callRepository.callState.collectAsState()
            val scope = rememberCoroutineScope()

            REACHTheme {
                CallScreen(
                    state = currentState,
                    onAccept = { callRepository.acceptCall() },
                    onReject = {
                        scope.launch() {
                            callRepository.rejectCall()
                            finish()
                        }
                    },
                    onHangUp = {
                        callRepository.endCall()
                        finish()
                    }
                )
            }
        }
    }

    private fun configureLockScreenFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }
}

@Composable
fun CallScreen(
    state: CallState,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onHangUp: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                text = "Unknown", // TODO: get this from state.peerId
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.secondary
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

                else -> {
                    /* TODO: Add Close screen */
                }
            }
        }
    }
}

@Preview(showBackground = false, showSystemUi = false)
@Composable
fun CallPreview() {
    CallScreen(
        state = CallState.Idle,
        onAccept = {},
        onReject = {},
        onHangUp = {},
    )
}