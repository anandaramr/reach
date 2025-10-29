package com.project.reach.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import com.project.reach.domain.models.MessageNotification

class NotificationHandler(
    private val context: Context
) {
    private val notificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    fun getForegroundNotification(stopServicePendingIntent: PendingIntent): Notification {
        return NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID)
            .setContentTitle("REACH is running")
            .setContentText("Listening for incoming messages")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .setGroup(null)
            .addAction(
                android.R.drawable.ic_media_pause,
                "Stop Listening",
                stopServicePendingIntent
            )
            .build()
    }

    fun pushMessageNotification(
        userId: String,
        username: String,
        messages: List<MessageNotification>,
    ) {
        val notificationId = userId.hashCode()

        val notification = NotificationCompat.Builder(
            context,
            MESSAGE_NOTIFICATION_CHANNEL
        )
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

    private fun getNotificationStyle(
        username: String,
        messages: List<MessageNotification>
    ): NotificationCompat.MessagingStyle {
        val currentUser = getPerson("me")
        val sender = getPerson(username)
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

    private fun getPerson(username: String) = Person.Builder()
        .setName(username)
        .build()

    companion object {
        const val FOREGROUND_NOTIFICATION_ID = 1
        const val FOREGROUND_CHANNEL_ID = "foreground_notification_channel"
        const val MESSAGE_NOTIFICATION_CHANNEL = "message_notification_channel"
    }
}