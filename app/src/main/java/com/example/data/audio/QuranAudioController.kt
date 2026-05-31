package com.example.data.audio

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class QuranAudioController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val playlistBuilder: SurahPlaylistBuilder,
    private val playbackResumeStore: PlaybackResumeStore,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var mediaController: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null

    private var currentReciterId: String = ""
    private var currentSubfolder: String = ""
    private var currentSurahNumber: Int = 1
    private var currentAyahNumber: Int = 1
    private var currentVerseCount: Int = 1
    private var currentSurahName: String = ""
    private var currentReciterName: String = ""
    private var playbackSpeed: Float = 1f
    private var playlistLoadedForSurah: Int = 0
    private var playlistLoadedForReciter: String = ""

    private var onAyahEndedListener: ((surahNumber: Int, nextAyah: Int?) -> Unit)? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0f)
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    private val _playingSurahNumber = MutableStateFlow(0)
    val playingSurahNumber: StateFlow<Int> = _playingSurahNumber.asStateFlow()

    private val _playingAyahNumber = MutableStateFlow(0)
    val playingAyahNumber: StateFlow<Int> = _playingAyahNumber.asStateFlow()

    private val _positionMs = MutableStateFlow(0L)
    val positionMs: StateFlow<Long> = _positionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private var progressJob: Job? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
            if (isPlaying) {
                startProgressUpdates()
            } else {
                stopProgressUpdates()
                updateProgress()
                savePlaybackSnapshot()
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                _isPlaying.value = false
                onAyahEndedListener?.invoke(currentSurahNumber, null)
            }
            updateProgress()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            syncAyahFromPlayer(mediaItem)
            updateProgress()
            savePlaybackSnapshot()
        }

        override fun onEvents(player: Player, events: Player.Events) {
            updateProgress()
        }
    }

    fun setOnAyahEndedListener(listener: (surahNumber: Int, nextAyah: Int?) -> Unit) {
        onAyahEndedListener = listener
    }

    fun connect() {
        if (mediaController != null || controllerFuture != null) return
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MediaPlaybackService::class.java),
        )
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener(
            {
                try {
                    val controller = controllerFuture?.get()
                        ?: error("MediaController future returned null")
                    mediaController = controller
                    controller.addListener(playerListener)
                    syncFromController(controller)
                } catch (throwable: Throwable) {
                    controllerFuture = null
                }
            },
            MoreExecutors.directExecutor(),
        )
    }

    fun release() {
        shutdown()
    }

    fun shutdown() {
        stopProgressUpdates()
        savePlaybackSnapshot()
        mediaController?.let { controller ->
            if (controller.isPlaying) {
                controller.pause()
            }
            savePlaybackSnapshot()
            controller.stop()
            controller.clearMediaItems()
            controller.removeListener(playerListener)
            controller.release()
        }
        mediaController = null
        controllerFuture = null
        playlistLoadedForSurah = 0
        playlistLoadedForReciter = ""
        _isPlaying.value = false
        _playbackProgress.value = 0f
        _positionMs.value = 0L
        _durationMs.value = 0L
        _playingSurahNumber.value = 0
        _playingAyahNumber.value = 0
    }

    private suspend fun getController(): MediaController {
        connect()
        mediaController?.let { return it }
        val future = controllerFuture ?: error("MediaController connection not started")
        return suspendCancellableCoroutine { continuation ->
            future.addListener(
                {
                    try {
                        val controller = future.get()
                        mediaController = controller
                        controller.addListener(playerListener)
                        syncFromController(controller)
                        continuation.resume(controller)
                    } catch (throwable: Throwable) {
                        controllerFuture = null
                        continuation.resumeWithException(throwable)
                    }
                },
                ContextCompat.getMainExecutor(context),
            )
        }
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
        scope.launch {
            val controller = getController()

            currentReciterId = reciterId
            currentSubfolder = subfolder
            currentSurahNumber = surahNumber
            currentAyahNumber = ayahNumber
            currentVerseCount = verseCount
            currentSurahName = surahName
            currentReciterName = reciterName

            _playingSurahNumber.value = surahNumber
            _playingAyahNumber.value = ayahNumber
            _playbackProgress.value = 0f
            _positionMs.value = 0L
            _durationMs.value = 0L

            val samePlaylistContext = playlistLoadedForSurah == surahNumber &&
                playlistLoadedForReciter == reciterId &&
                controller.mediaItemCount == verseCount

            if (samePlaylistContext) {
                val targetIndex = (ayahNumber - 1).coerceIn(0, verseCount - 1)
                controller.seekTo(targetIndex, 0L)
                if (autoPlay) {
                    controller.play()
                } else {
                    controller.pause()
                }
                syncAyahFromPlayer(controller.currentMediaItem)
                updateProgress()
                savePlaybackSnapshot()
                return@launch
            }

            val items = playlistBuilder.buildSurahPlaylist(
                reciterId = reciterId,
                subfolder = subfolder,
                surahNumber = surahNumber,
                surahName = surahName,
                reciterName = reciterName,
                verseCount = verseCount,
            )
            val startIndex = (ayahNumber - 1).coerceIn(0, items.lastIndex.coerceAtLeast(0))

            controller.setMediaItems(items, startIndex, 0L)
            controller.playbackParameters = PlaybackParameters(playbackSpeed)
            controller.prepare()

            playlistLoadedForSurah = surahNumber
            playlistLoadedForReciter = reciterId

            if (autoPlay) {
                controller.play()
            } else {
                controller.pause()
            }

            syncAyahFromPlayer(controller.currentMediaItem)
            updateProgress()
            savePlaybackSnapshot()
        }
    }

    fun setPlaying(playing: Boolean) {
        scope.launch {
            if (playing) {
                val controller = getController()
                if (controller.mediaItemCount == 0) return@launch
                controller.play()
            } else {
                mediaController?.pause()
                savePlaybackSnapshot()
            }
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        playbackSpeed = speed
        mediaController?.playbackParameters = PlaybackParameters(speed)
        savePlaybackSnapshot()
    }

    fun playNextAyah() {
        val controller = mediaController ?: return
        if (controller.hasNextMediaItem()) {
            controller.seekToNextMediaItem()
        }
    }

    fun playPreviousAyah() {
        val controller = mediaController ?: return
        if (controller.currentPosition > 3_000L) {
            controller.seekTo(0L)
        } else if (controller.hasPreviousMediaItem()) {
            controller.seekToPreviousMediaItem()
        } else {
            controller.seekTo(0L)
        }
    }

    fun stop() {
        stopProgressUpdates()
        mediaController?.stop()
        mediaController?.clearMediaItems()
        playlistLoadedForSurah = 0
        playlistLoadedForReciter = ""
        _isPlaying.value = false
        _playbackProgress.value = 0f
        _positionMs.value = 0L
        _durationMs.value = 0L
        _playingSurahNumber.value = 0
        _playingAyahNumber.value = 0
        playbackResumeStore.clear()
    }

    private fun syncFromController(controller: MediaController) {
        _isPlaying.value = controller.isPlaying
        syncAyahFromPlayer(controller.currentMediaItem)
        updateProgress()
    }

    private fun syncAyahFromPlayer(mediaItem: MediaItem?) {
        val ayahNumber = playlistBuilder.ayahNumberFromMediaId(mediaItem?.mediaId) ?: return
        if (ayahNumber == currentAyahNumber) return

        currentAyahNumber = ayahNumber
        _playingAyahNumber.value = ayahNumber
        _playingSurahNumber.value = currentSurahNumber
        onAyahEndedListener?.invoke(currentSurahNumber, ayahNumber)
    }

    private fun savePlaybackSnapshot() {
        val controller = mediaController ?: return
        if (controller.mediaItemCount == 0 || currentSurahNumber <= 0) return

        playbackResumeStore.save(
            PlaybackSnapshot(
                reciterId = currentReciterId,
                subfolder = currentSubfolder,
                surahNumber = currentSurahNumber,
                ayahNumber = currentAyahNumber,
                verseCount = currentVerseCount,
                surahName = currentSurahName,
                reciterName = currentReciterName,
                mediaItemIndex = controller.currentMediaItemIndex.coerceAtLeast(0),
                positionMs = controller.currentPosition.coerceAtLeast(0L),
                durationMs = controller.duration.takeIf { it > 0L && it != C.TIME_UNSET } ?: 0L,
                playbackSpeed = playbackSpeed,
            ),
        )
    }

    private fun startProgressUpdates() {
        if (progressJob?.isActive == true) return
        progressJob = scope.launch {
            while (true) {
                updateProgress()
                val controller = mediaController ?: break
                if (!controller.isPlaying) break
                delay(50)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun updateProgress() {
        val controller = mediaController ?: return
        val duration = controller.duration
        val position = controller.currentPosition

        if (duration <= 0L || duration == C.TIME_UNSET) {
            _positionMs.value = 0L
            _durationMs.value = 0L
            _playbackProgress.value = 0f
            return
        }

        val clampedPosition = position.coerceIn(0L, duration)
        _positionMs.value = clampedPosition
        _durationMs.value = duration
        _playbackProgress.value = (clampedPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    }
}
