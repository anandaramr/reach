package com.project.reach.ui.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.project.reach.permission.PermissionHandler
import com.project.reach.service.ForegroundService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity: ComponentActivity() {
    private lateinit var permissionHandler: PermissionHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionHandler = PermissionHandler(this)
        permissionHandler.onNotificationPermissionGranted(
            onGranted = ::startForegroundService
        )

        enableEdgeToEdge()
        setContent {
            ReachApp()
        }
    }

    private fun startForegroundService() {
        Intent(applicationContext, ForegroundService::class.java).also {
            it.action = ForegroundService.ACTION_START
            startService(it)
        }
    }
}
