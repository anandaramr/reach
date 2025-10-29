package com.project.reach.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import com.project.reach.domain.contracts.IMessageRepository
import com.project.reach.domain.contracts.INetworkRepository
import com.project.reach.domain.models.MessageNotification
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

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        scope.launch {
            messageRepository.notifications.collect { event ->
                when (event) {
                    is NotificationEvent.Message -> {
                        pushMessageNotification(
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

        val notification = getForegroundNotification()
        startForeground(FOREGROUND_NOTIFICATION_ID, notification)

        networkRepository.startDiscovery()
    }

    private fun stop() {
        if (!isStarted) return
        isStarted = false

        networkRepository.release()
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
            .setGroup(null)
            .addAction(android.R.drawable.ic_media_pause, "Stop Listening", stopPendingIntent)
            .build()
    }

    private fun pushMessageNotification(
        userId: String,
        username: String,
        messages: List<MessageNotification>,
    ) {
        val notificationId = userId.hashCode()

        // TODO: handle grouping messages from a single user using `MessagingStyle`
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(this, MESSAGE_NOTIFICATION_CHANNEL)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(username)
            .setStyle(getNotificationStyle(username, messages))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setGroup("message")
            .setWhen(messages.last().timeStamp)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        stop()
    }

    private fun getNotificationStyle(
        username: String,
        messages: List<MessageNotification>
    ): NotificationCompat.MessagingStyle {
        val currentUser = Person.Builder()
            .setName("me")
            .build()

        val sender = Person.Builder()
            .setName(username)
            .build()

        val messagingStyle = NotificationCompat.MessagingStyle(currentUser)
        messages.forEachIndexed { idx, msg ->
            messagingStyle.addMessage(
                NotificationCompat.MessagingStyle.Message(
                    msg.text,
                    msg.timeStamp,
                    sender
                )
            )
        }

        return messagingStyle
    }

    companion object {
        const val FOREGROUND_NOTIFICATION_ID = 1
        const val FOREGROUND_CHANNEL_ID = "foreground_notification_channel"
        const val MESSAGE_NOTIFICATION_CHANNEL = "message_notification_channel"
        const val ACTION_START = "com.project.reach.START_SERVICE"
        const val ACTION_STOP = "com.project.reach.STOP_SERVICE"
    }
}