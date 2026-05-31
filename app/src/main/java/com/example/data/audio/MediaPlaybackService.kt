package com.example.data.audio

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.media3.common.C
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.MainActivity
import com.example.R
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@UnstableApi
@AndroidEntryPoint
class MediaPlaybackService : MediaSessionService() {

    @Inject
    lateinit var playbackResumeStore: PlaybackResumeStore

    @Inject
    lateinit var playlistBuilder: SurahPlaylistBuilder

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (!isPlaying) {
                saveSnapshotFromPlayer()
            }
        }

        override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
            saveSnapshotFromPlayer()
        }
    }

    private val playbackSessionCallback = object : MediaSession.Callback {
        override fun onPlaybackResumption(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            isForPlayback: Boolean,
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
            val snapshot = playbackResumeStore.load()
                ?: return Futures.immediateFailedFuture(
                    IllegalStateException("No saved playback state"),
                )
            val future = SettableFuture.create<MediaSession.MediaItemsWithStartPosition>()
            serviceScope.launch {
                try {
                    player?.playbackParameters = PlaybackParameters(snapshot.playbackSpeed)
                    future.set(playlistBuilder.buildResumption(snapshot, isForPlayback))
                } catch (throwable: Throwable) {
                    future.setException(throwable)
                }
            }
            return future
        }
    }

    override fun onCreate() {
        super.onCreate()
        ensureNotificationChannel()
        setMediaNotificationProvider(
            DefaultMediaNotificationProvider(
                this,
                { NOTIFICATION_ID },
                NOTIFICATION_CHANNEL_ID,
                R.string.media_playback_notification_title,
            ),
        )

        val exoPlayer = ExoPlayer.Builder(this).build().also { it.addListener(playerListener) }
        player = exoPlayer
        mediaSession = MediaSession.Builder(this, exoPlayer)
            .setId("QuranMediaSession")
            .setSessionActivity(createSessionActivityPendingIntent())
            .setCallback(playbackSessionCallback)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    @OptIn(UnstableApi::class)
    override fun onTaskRemoved(rootIntent: Intent?) {
        saveSnapshotFromPlayer()
        pauseAllPlayersAndStopSelf()
    }

    override fun onDestroy() {
        releaseMediaSession()
        super.onDestroy()
    }

    private fun releaseMediaSession() {
        player?.removeListener(playerListener)
        mediaSession?.run {
            player.stop()
            player.clearMediaItems()
            player.release()
            release()
        }
        mediaSession = null
        player = null
    }

    private fun saveSnapshotFromPlayer() {
        val activePlayer = player ?: return
        val existing = playbackResumeStore.load() ?: return
        if (activePlayer.mediaItemCount == 0) return

        val ayahNumber = playlistBuilder.ayahNumberFromMediaId(activePlayer.currentMediaItem?.mediaId)
            ?: existing.ayahNumber

        playbackResumeStore.save(
            existing.copy(
                ayahNumber = ayahNumber,
                mediaItemIndex = activePlayer.currentMediaItemIndex.coerceAtLeast(0),
                positionMs = activePlayer.currentPosition.coerceAtLeast(0L),
                durationMs = activePlayer.duration.takeIf { it > 0L && it != C.TIME_UNSET } ?: existing.durationMs,
            ),
        )
    }

    private fun createSessionActivityPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            getString(R.string.media_playback_notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = getString(R.string.media_playback_notification_channel_description)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "quran_media_playback"
        private const val NOTIFICATION_ID = 1001
    }
}
