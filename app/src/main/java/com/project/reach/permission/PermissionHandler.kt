package com.project.reach.permission

import android.Manifest
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

class PermissionHandler(
    private val activity: ComponentActivity
) {
    fun onNotificationPermissionGranted(onGranted: () -> Unit, onFailure: (() -> Unit)? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onGrantedCallback = onGranted
            onFailureCallback = onFailure
            notificationLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
        } else {
            onGranted()
        }
    }

    private val notificationLauncher =
        activity.registerForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions[Manifest.permission.POST_NOTIFICATIONS] == true) {
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