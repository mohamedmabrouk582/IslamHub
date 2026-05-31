package com.example.data.audio

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.example.data.repository.AyahAudioRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Singleton
class QuranAudioController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ayahAudioRepository: AyahAudioRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null

    private var currentReciterId: String = ""
    private var currentSubfolder: String = ""
    private var currentSurahNumber: Int = 1
    private var currentAyahNumber: Int = 1
    private var currentVerseCount: Int = 1
    private var currentSurahName: String = ""
    private var currentReciterName: String = ""
    private var playbackSpeed: Float = 1f

    private var onAyahEndedListener: ((surahNumber: Int, nextAyah: Int?) -> Unit)? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0f)
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    private val _positionMs = MutableStateFlow(0L)
    val positionMs: StateFlow<Long> = _positionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                handleAyahEnded()
            }
            updateProgress()
        }

        override fun onEvents(player: Player, events: Player.Events) {
            updateProgress()
        }
    }

    fun setOnAyahEndedListener(listener: (surahNumber: Int, nextAyah: Int?) -> Unit) {
        onAyahEndedListener = listener
    }

    fun ensurePlayerInitialized() {
        if (player != null) return
        val exoPlayer = ExoPlayer.Builder(context).build().apply {
            addListener(playerListener)
        }
        player = exoPlayer
        mediaSession = MediaSession.Builder(context, exoPlayer)
            .setId("QuranMediaSession")
            .build()
    }

    fun getMediaSession(): MediaSession? = mediaSession

    private fun ensureServiceStarted() {
        val intent = Intent(context, MediaPlaybackService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }

    fun playAyah(
        reciterId: String,
        subfolder: String,
        surahNumber: Int,
        ayahNumber: Int,
        verseCount: Int,
        surahName: String,
        reciterName: String,
        autoPlay: Boolean = true,
    ) {
        ensurePlayerInitialized()

        currentReciterId = reciterId
        currentSubfolder = subfolder
        currentSurahNumber = surahNumber
        currentAyahNumber = ayahNumber
        currentVerseCount = verseCount
        currentSurahName = surahName
        currentReciterName = reciterName

        scope.launch {
            val uri = ayahAudioRepository.resolvePlaybackUri(
                reciterId = reciterId,
                subfolder = subfolder,
                surahNumber = surahNumber,
                ayahNumber = ayahNumber,
            )
            val mediaItem = MediaItem.Builder()
                .setUri(uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle("$surahName - Ayah $ayahNumber")
                        .setArtist(reciterName)
                        .build(),
                )
                .build()

            player?.apply {
                setMediaItem(mediaItem)
                playbackParameters = PlaybackParameters(playbackSpeed)
                prepare()
            }

            ensureServiceStarted()

            player?.apply {
                if (autoPlay) {
                    play()
                } else {
                    pause()
                }
            }
            updateProgress()
        }
    }

    fun setPlaying(playing: Boolean) {
        ensurePlayerInitialized()
        if (playing) {
            if (player?.mediaItemCount == 0) return
            ensureServiceStarted()
            player?.play()
        } else {
            player?.pause()
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        playbackSpeed = speed
        player?.playbackParameters = PlaybackParameters(speed)
    }

    fun playNextAyah() {
        val nextAyah = currentAyahNumber + 1
        if (nextAyah <= currentVerseCount) {
            currentAyahNumber = nextAyah
            playCurrentAyah()
        }
    }

    fun playPreviousAyah() {
        val previousAyah = currentAyahNumber - 1
        if (previousAyah >= 1) {
            currentAyahNumber = previousAyah
            playCurrentAyah()
        }
    }

    private fun playCurrentAyah(autoPlay: Boolean = true) {
        playAyah(
            reciterId = currentReciterId,
            subfolder = currentSubfolder,
            surahNumber = currentSurahNumber,
            ayahNumber = currentAyahNumber,
            verseCount = currentVerseCount,
            surahName = currentSurahName,
            reciterName = currentReciterName,
            autoPlay = autoPlay,
        )
    }

    fun stop() {
        player?.stop()
        _isPlaying.value = false
        _playbackProgress.value = 0f
        _positionMs.value = 0L
        _durationMs.value = 0L
    }

    private fun handleAyahEnded() {
        val nextAyah = currentAyahNumber + 1
        if (nextAyah <= currentVerseCount) {
            currentAyahNumber = nextAyah
            playCurrentAyah()
            onAyahEndedListener?.invoke(currentSurahNumber, nextAyah)
        } else {
            _isPlaying.value = false
            onAyahEndedListener?.invoke(currentSurahNumber, null)
        }
    }

    private fun updateProgress() {
        val activePlayer = player ?: return
        val duration = activePlayer.duration
        val position = activePlayer.currentPosition
        _positionMs.value = position.coerceAtLeast(0L)
        _durationMs.value = duration.coerceAtLeast(0L)
        _playbackProgress.value = if (duration > 0L) {
            (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
    }
}
