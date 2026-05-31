package com.example.data.audio

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import com.example.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MediaPlaybackService : MediaSessionService() {

    @Inject
    lateinit var audioController: QuranAudioController

    override fun onCreate() {
        super.onCreate()
        audioController.ensurePlayerInitialized()
        startForegroundImmediately()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return audioController.getMediaSession()
    }

    private fun startForegroundImmediately() {
        ensureNotificationChannel()
        val session = audioController.getMediaSession()
        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.media_playback_notification_title))
            .setContentText(getString(R.string.media_playback_notification_text))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)

        if (session != null) {
            builder.setStyle(MediaStyleNotificationHelper.MediaStyle(session))
        }

        val notification = builder.build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
            )
        } else {
            @Suppress("DEPRECATION")
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            getString(R.string.media_playback_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.media_playback_notification_channel_description)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "quran_media_playback"
        private const val NOTIFICATION_ID = 1001
    }
}
