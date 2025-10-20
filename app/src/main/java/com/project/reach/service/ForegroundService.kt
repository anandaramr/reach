package com.project.reach.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.project.reach.domain.contracts.IAppRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ForegroundService: Service() {

    @Inject
    lateinit var appRepository: IAppRepository

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }

        return START_STICKY
    }

    private var isStarted = false

    private fun start() {
        if (isStarted) return
        isStarted = true

        val notification = getForegroundNotification()
        startForeground(FOREGROUND_NOTIFICATION_ID, notification)

        appRepository.startDiscovery()
    }

    private fun stop() {
        appRepository.stopDiscovery()
        appRepository.release()
        stopSelf()
    }

    private fun getForegroundNotification(): Notification {
        val stopIntent = Intent(this, ForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, FOREGROUND_CHANNEL_ID)
            .setContentTitle("REACH is running")
            .setContentText("Listening for incoming messages")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_media_pause, "Stop Listening", stopPendingIntent)
            .build()
    }

    companion object {
        const val FOREGROUND_NOTIFICATION_ID = 1
        const val FOREGROUND_CHANNEL_ID = "foreground_notification_channel"
        const val ACTION_START = "com.project.reach.START_SERVICE"
        const val ACTION_STOP = "com.project.reach.STOP_SERVICE"
    }
}