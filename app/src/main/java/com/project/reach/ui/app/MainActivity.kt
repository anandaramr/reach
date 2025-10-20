package com.project.reach.ui.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.project.reach.permission.PermissionHandler
import com.project.reach.service.ForegroundServiceManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity: ComponentActivity() {
    private lateinit var permissionHandler: PermissionHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startReachService()

        enableEdgeToEdge()
        setContent {
            ReachApp()
        }
    }

    private fun startReachService() {
        permissionHandler = PermissionHandler(this)
        permissionHandler.onNotificationPermissionGranted(
            onGranted = { ForegroundServiceManager.startService(applicationContext) }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        ForegroundServiceManager.stopService(applicationContext)
    }
}
