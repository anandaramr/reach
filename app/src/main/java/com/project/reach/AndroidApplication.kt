package com.project.reach

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.project.reach.service.ForegroundService
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AndroidApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        val listenNotification = NotificationChannel(
            ForegroundService.FOREGROUND_CHANNEL_ID,
            "Listen for messages",
            NotificationManager.IMPORTANCE_LOW
        )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(listenNotification)
    }

}