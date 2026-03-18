package com.project.reach.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import com.project.reach.domain.models.MessageNotification
import com.project.reach.ui.app.CallActivity

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

    fun getCallNotification(username: String, isIncoming: Boolean): Notification {
        val person = Person.Builder()
            .setName(username)
            .setImportant(true)
            .build()

        val answerIntent = getCallActionPendingIntent("ACTION_ACCEPT_CALL")

        val declineIntent = getCallActionPendingIntent("ACTION_REJECT_CALL")

        val hangUpIntent = getCallActionPendingIntent("ACTION_END_CALL")

        val fullScreenIntent = Intent(context, CallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, 0, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val style = if (isIncoming) {
            NotificationCompat.CallStyle.forIncomingCall(
                person,
                declineIntent,
                answerIntent
            )
        } else {
            NotificationCompat.CallStyle.forOngoingCall(
                person,
                hangUpIntent
            )
        }

        return NotificationCompat.Builder(context, CALL_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.sym_action_call)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .setAutoCancel(false)
            .setStyle(style)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .build()
    }

    fun pushMessageNotification(
        userId: String,
        username: String,
        messages: List<MessageNotification>,
    ) {
        if (messages.isEmpty()) return
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

    private fun getCallActionPendingIntent(actionName: String): PendingIntent {
        val intent = Intent(context, CallActionReceiver::class.java).apply {
            action = actionName
        }
        return PendingIntent.getBroadcast(
            context, actionName.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val FOREGROUND_NOTIFICATION_ID = 1
        const val CALL_NOTIFICATION_ID = 2
        const val FOREGROUND_CHANNEL_ID = "foreground_notification_channel"
        const val CALL_CHANNEL_ID = "call_notification_channel"
        const val MESSAGE_NOTIFICATION_CHANNEL = "message_notification_channel"
    }
}