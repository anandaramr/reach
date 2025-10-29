package com.project.reach.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.project.reach.domain.contracts.IMessageRepository
import com.project.reach.domain.contracts.INetworkRepository
import com.project.reach.domain.models.NotificationEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ForegroundService: Service() {

    @Inject
    lateinit var networkRepository: INetworkRepository

    @Inject
    lateinit var messageRepository: IMessageRepository

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val notificationHandler = NotificationHandler(this.applicationContext)

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        scope.launch {
            messageRepository.notifications.collect { event ->
                when (event) {
                    is NotificationEvent.Message -> {
                        notificationHandler.pushMessageNotification(
                            userId = event.userId,
                            username = event.username,
                            messages = event.messages,
                        )
                    }
                }
            }
        }
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

        val stopIntent = Intent(this, ForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = notificationHandler.getForegroundNotification(stopPendingIntent)
        startForeground(NotificationHandler.FOREGROUND_NOTIFICATION_ID, notification)

        networkRepository.startDiscovery()
    }

    private fun stop() {
        if (!isStarted) return
        isStarted = false

        networkRepository.release()
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stop()
    }

    companion object {
        const val ACTION_START = "com.project.reach.START_SERVICE"
        const val ACTION_STOP = "com.project.reach.STOP_SERVICE"
    }
}