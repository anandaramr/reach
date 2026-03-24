package com.project.reach.ui.app

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.project.reach.domain.contracts.ICallRepository
import com.project.reach.domain.models.CallState
import com.project.reach.permission.PermissionHandler
import com.project.reach.ui.screens.calls.CallScreen
import com.project.reach.ui.theme.REACHTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CallActivity: ComponentActivity() {

    @Inject
    lateinit var callRepository: ICallRepository

    private val permissionHandler = PermissionHandler(
        activity = this
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configureLockScreenFlags()

        setContent {
            val currentState by callRepository.callState.collectAsState()
            if (currentState == CallState.Idle) finish()

            val scope = rememberCoroutineScope()
            val context = LocalContext.current

            REACHTheme {
                CallScreen(
                    state = currentState,
                    onAccept = {
                        permissionHandler.onMicrophonePermissionGranted(
                            onGranted = {
                                scope.launch {
                                    callRepository.acceptCall()
                                }
                            },
                            onFailure = {
                                Toast.makeText(
                                    context,
                                    "Enable mic to start call",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        )
                    },
                    onReject = {
                        scope.launch {
                            callRepository.rejectCall()
                            finish()
                        }
                    },
                    onHangUp = {
                        callRepository.endCall()
                        finish()
                    },
                    onCancel = {
                        callRepository.resetCall()
                        finish()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        callRepository.resetCall()
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
