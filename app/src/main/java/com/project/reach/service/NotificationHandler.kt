package com.project.reach.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.net.toUri
import com.project.reach.domain.models.MessageNotification
import com.project.reach.ui.app.CallActivity
import com.project.reach.ui.app.MainActivity
import com.project.reach.ui.screens.chat.ChatScreenDestination
import com.project.reach.util.debug
import com.project.reach.R
import com.project.reach.domain.models.CallState

class NotificationHandler(
    private val context: Context
) {
    private val notificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    fun getForegroundNotification(onStopService: PendingIntent): Notification {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent =
            PendingIntent.getActivity(context, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID)
            .setContentTitle("REACH is running")
            .setContentText("Listening for incoming messages")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setGroup(null)
            .addAction(
                android.R.drawable.ic_media_pause,
                "Stop Listening",
                onStopService
            )
            .build()
    }

    fun getCallNotification(username: String, callState: CallState): Notification {
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

        val style = if (callState is CallState.Incoming) {
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

        val builder = NotificationCompat.Builder(context, CALL_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .setAutoCancel(false)
            .setStyle(style)

        @SuppressLint("FullScreenIntentPolicy")
        if (callState is CallState.Incoming) {
            val canUseFullScreen =
                Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE || notificationManager.canUseFullScreenIntent()
            if (canUseFullScreen) {
                builder.setFullScreenIntent(fullScreenPendingIntent, true)
            } else {
                debug("[Notification] Unable to use setFullScreenIntent during call")
                builder.setContentIntent(fullScreenPendingIntent)
            }
        } else {
            builder.setContentIntent(fullScreenPendingIntent)
        }
        return builder.build()
    }

    fun pushMessageNotification(
        userId: String,
        username: String,
        messages: List<MessageNotification>,
    ) {
        if (messages.isEmpty()) return
        val notificationId = userId.hashCode()

        val openChatScreenIntent = Intent(
            Intent.ACTION_VIEW,
            "reach://${ChatScreenDestination.createDeepLinkUri(userId)}".toUri(),
            context,
            MainActivity::class.java
        )
        val pendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(openChatScreenIntent)
            getPendingIntent(
                userId.hashCode(),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notification = NotificationCompat.Builder(
            context,
            MESSAGE_NOTIFICATION_CHANNEL
        )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(username)
            .setStyle(getNotificationStyle(username, messages))
            .setContentIntent(pendingIntent)
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