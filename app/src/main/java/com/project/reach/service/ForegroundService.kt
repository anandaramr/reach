package com.project.reach.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.project.reach.domain.contracts.ICallRepository
import com.project.reach.domain.contracts.IMessageRepository
import com.project.reach.domain.contracts.INetworkRepository
import com.project.reach.domain.models.CallState
import com.project.reach.domain.models.NotificationEvent
import com.project.reach.ui.app.CallActivity
import com.project.reach.util.debug
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ForegroundService: Service() {

    @Inject
    lateinit var networkRepository: INetworkRepository

    @Inject
    lateinit var messageRepository: IMessageRepository

    @Inject
    lateinit var callRepository: ICallRepository

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val notificationHandler = NotificationHandler(this)

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        if (action == null) {
            start()
        } else {
            when (action) {
                ACTION_START -> start()
                ACTION_STOP -> stop()
            }
        }

        return START_STICKY
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

        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        if (powerManager.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)) {
            wakeLock = powerManager.newWakeLock(
                PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                "Reach:ForegroundService"
            )
        }

        scope.launch {
            callRepository.callState.collect { state ->
                debug("new state: $state")
                when (state) {
                    is CallState.Incoming -> {
                        startRinging()
                        requestAudioFocus()
                        onStartCall(state.username, state)
                    }

                    is CallState.Outgoing -> {
                        requestAudioFocus()
                        startProximitySensor()
                        onStartCall(state.username, state)
                        launchCallActivity()
                    }

                    CallState.Idle -> {
                        stopRinging()
                        abandonAudioFocus()
                        stopProximitySensor()
                        startForegroundOperations()
                    }

                    is CallState.Disconnected -> {
                        stopRinging()
                        abandonAudioFocus()
                        stopProximitySensor()
                        startForegroundOperations()
                    }

                    is CallState.Connected -> {
                        stopRinging()
                        startProximitySensor()
                        onStartCall(state.username, state)
                    }
                }
            }
        }
    }

    private fun onStartCall(username: String, callState: CallState) {
        val notification = notificationHandler.getCallNotification(username, callState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                startForeground(
                    NotificationHandler.CALL_NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                )
            } else {
                startForeground(
                    NotificationHandler.CALL_NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
                )
            }
        } else {
            startForeground(NotificationHandler.CALL_NOTIFICATION_ID, notification)
        }
    }

    private fun startRinging() {
        startVibration()
        startRingtone()
    }

    private fun stopRinging() {
        stopVibration()
        stopRingtone()
    }

    var mediaPlayer: MediaPlayer? = null
    private fun startRingtone() {
        if (mediaPlayer != null) {
            debug("Ringtone already playing")
            return
        }

        try {
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@ForegroundService, ringtoneUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            debug("Failed to start ringtone: ${e.message}")
        }
    }

    private fun stopRingtone() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun startVibration() {
        val vibrator = getVibrator()
        val pattern = longArrayOf(0, 1000, 1000)
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
    }

    private fun getVibrator(): Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("Deprecation")
        getSystemService(VIBRATOR_SERVICE) as Vibrator
    }

    private fun stopVibration() {
        getVibrator().cancel()
    }

    var audioFocusRequest: AudioFocusRequest? = null
    val audioManager by lazy { getSystemService(AUDIO_SERVICE) as AudioManager }

    private fun requestAudioFocus() {
        val focusRequest =
            AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .build()
        audioFocusRequest = focusRequest
        audioManager.requestAudioFocus(focusRequest)
    }

    private fun abandonAudioFocus() {
        audioFocusRequest?.let { request ->
            audioManager.abandonAudioFocusRequest(request)
            audioFocusRequest = null
        }
    }

    @SuppressLint("WakelockTimeout")
    private fun startProximitySensor() {
        if (wakeLock?.isHeld == false) {
            wakeLock?.acquire()
        }
    }

    private fun stopProximitySensor() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }

    private fun launchCallActivity() {
        val intent = Intent(this, CallActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun start() {
        if (ForegroundServiceManager.isRunning()) return
        ForegroundServiceManager.setServiceState(true)
        startForegroundOperations()
        networkRepository.startDiscovery()
    }

    private fun startForegroundOperations() {
        val stopIntent = Intent(this, ForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = notificationHandler.getForegroundNotification(stopPendingIntent)
        startForeground(NotificationHandler.FOREGROUND_NOTIFICATION_ID, notification)
    }

    private fun stop() {
        if (!ForegroundServiceManager.isRunning()) return
        ForegroundServiceManager.setServiceState(false)

        networkRepository.release()
        stopSelf()
    }

    override fun onDestroy() {
        stopRinging()
        abandonAudioFocus()
        stopProximitySensor()
        scope.cancel()
        stop()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.project.reach.START_SERVICE"
        const val ACTION_STOP = "com.project.reach.STOP_SERVICE"
    }
}