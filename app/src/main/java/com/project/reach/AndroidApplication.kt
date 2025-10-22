package com.project.reach

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.project.reach.service.ForegroundService
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AndroidApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        val foregroundNotification = NotificationChannel(
            ForegroundService.FOREGROUND_CHANNEL_ID,
            "Background service channel",
            NotificationManager.IMPORTANCE_LOW
        )

        val messageNotification = NotificationChannel(
            ForegroundService.MESSAGE_NOTIFICATION_CHANNEL,
            "Notify new messages",
            NotificationManager.IMPORTANCE_HIGH
        )

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(foregroundNotification)
        notificationManager.createNotificationChannel(messageNotification)
    }

}