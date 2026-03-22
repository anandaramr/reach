package com.project.reach.permission

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.project.reach.util.debug

class PermissionHandler(
    private val activity: ComponentActivity
) {
    fun onNotificationPermissionGranted(onGranted: () -> Unit, onFailure: (() -> Unit)? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onGrantedCallback = onGranted
            onFailureCallback = onFailure
            notificationLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.RECORD_AUDIO))
        } else {
            onGranted()
        }
    }

    fun onMicrophonePermissionGranted(onGranted: () -> Unit, onFailure: (() -> Unit)? = null) {
        onFailureCallback = onFailure
        onGrantedCallback = onGranted
        val status = ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)

        if (status == PackageManager.PERMISSION_GRANTED) {
            debug("Microphone already granted, proceeding...")
            onGranted.invoke()
        } else {
            debug("Requesting microphone permission...")
            microphonePermissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
        }
    }

    private val notificationLauncher =
        activity.registerForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions[Manifest.permission.POST_NOTIFICATIONS] == true && permissions[Manifest.permission.RECORD_AUDIO] == true) {
                onGrantedCallback?.invoke()
            } else {
                onFailureCallback?.invoke()
            }

            onGrantedCallback = null
            onFailureCallback = null
        }

    private val microphonePermissionLauncher =
        activity.registerForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions[Manifest.permission.RECORD_AUDIO] == true) {
                debug("Microphone permission granted")
                onGrantedCallback?.invoke()
            } else {
                onFailureCallback?.invoke()
            }

            onGrantedCallback = null
            onFailureCallback = null
        }

    private var onGrantedCallback: (() -> Unit)? = null
    private var onFailureCallback: (() -> Unit)? = null
}