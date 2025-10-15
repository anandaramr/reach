package com.project.reach.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat

class ForegroundService: Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stopSelf()
        }

        return START_STICKY
    }

    private fun start() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("REACH is running")
            .setContentText("REACH is listening for messages")
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "notification_channel"
        const val ACTION_START = "com.project.reach.START_SERVICE"
        const val ACTION_STOP = "com.project.reach.STOP_SERVICE"
    }
}