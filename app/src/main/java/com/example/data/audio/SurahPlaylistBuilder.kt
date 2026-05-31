package com.example.data.audio

import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaConstants
import androidx.media3.session.MediaSession
import com.example.data.repository.AyahAudioRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(UnstableApi::class)
@Singleton
class SurahPlaylistBuilder @Inject constructor(
    private val ayahAudioRepository: AyahAudioRepository,
) {
    suspend fun buildSurahPlaylist(
        reciterId: String,
        subfolder: String,
        surahNumber: Int,
        surahName: String,
        reciterName: String,
        verseCount: Int,
        completionExtrasAyah: Int? = null,
        completionPositionMs: Long = 0L,
        completionDurationMs: Long = 0L,
    ): List<MediaItem> {
        return withContext(Dispatchers.IO) {
            (1..verseCount).map { ayahNumber ->
                buildPlayableMediaItem(
                    reciterId = reciterId,
                    subfolder = subfolder,
                    surahNumber = surahNumber,
                    surahName = surahName,
                    reciterName = reciterName,
                    verseCount = verseCount,
                    ayahNumber = ayahNumber,
                    completionExtrasAyah = completionExtrasAyah,
                    completionPositionMs = completionPositionMs,
                    completionDurationMs = completionDurationMs,
                )
            }
        }
    }

    fun buildMetadataOnlyPlaylist(snapshot: PlaybackSnapshot): List<MediaItem> {
        return (1..snapshot.verseCount).map { ayahNumber ->
            buildMetadataMediaItem(
                surahNumber = snapshot.surahNumber,
                surahName = snapshot.surahName,
                reciterName = snapshot.reciterName,
                verseCount = snapshot.verseCount,
                ayahNumber = ayahNumber,
                completionExtrasAyah = snapshot.ayahNumber,
                completionPositionMs = snapshot.positionMs,
                completionDurationMs = snapshot.durationMs,
            )
        }
    }

    suspend fun buildResumption(
        snapshot: PlaybackSnapshot,
        isForPlayback: Boolean,
    ): MediaSession.MediaItemsWithStartPosition {
        val startIndex = snapshot.mediaItemIndex.coerceIn(0, (snapshot.verseCount - 1).coerceAtLeast(0))
        val startPositionMs = snapshot.positionMs.coerceAtLeast(0L)
        val items = if (isForPlayback) {
            buildSurahPlaylist(
                reciterId = snapshot.reciterId,
                subfolder = snapshot.subfolder,
                surahNumber = snapshot.surahNumber,
                surahName = snapshot.surahName,
                reciterName = snapshot.reciterName,
                verseCount = snapshot.verseCount,
                completionExtrasAyah = snapshot.ayahNumber,
                completionPositionMs = snapshot.positionMs,
                completionDurationMs = snapshot.durationMs,
            )
        } else {
            buildMetadataOnlyPlaylist(snapshot)
        }
        return MediaSession.MediaItemsWithStartPosition(items, startIndex, startPositionMs)
    }

    private suspend fun buildPlayableMediaItem(
        reciterId: String,
        subfolder: String,
        surahNumber: Int,
        surahName: String,
        reciterName: String,
        verseCount: Int,
        ayahNumber: Int,
        completionExtrasAyah: Int?,
        completionPositionMs: Long,
        completionDurationMs: Long,
    ): MediaItem {
        val uri = ayahAudioRepository.resolvePlaybackUri(
            reciterId = reciterId,
            subfolder = subfolder,
            surahNumber = surahNumber,
            ayahNumber = ayahNumber,
        )
        return MediaItem.Builder()
            .setUri(uri)
            .setMediaId(mediaItemId(surahNumber, ayahNumber))
            .setMediaMetadata(
                buildMediaMetadata(
                    surahName = surahName,
                    reciterName = reciterName,
                    verseCount = verseCount,
                    ayahNumber = ayahNumber,
                    completionExtrasAyah = completionExtrasAyah,
                    completionPositionMs = completionPositionMs,
                    completionDurationMs = completionDurationMs,
                ),
            )
            .build()
    }

    private fun buildMetadataMediaItem(
        surahNumber: Int,
        surahName: String,
        reciterName: String,
        verseCount: Int,
        ayahNumber: Int,
        completionExtrasAyah: Int?,
        completionPositionMs: Long,
        completionDurationMs: Long,
    ): MediaItem {
        return MediaItem.Builder()
            .setMediaId(mediaItemId(surahNumber, ayahNumber))
            .setMediaMetadata(
                buildMediaMetadata(
                    surahName = surahName,
                    reciterName = reciterName,
                    verseCount = verseCount,
                    ayahNumber = ayahNumber,
                    completionExtrasAyah = completionExtrasAyah,
                    completionPositionMs = completionPositionMs,
                    completionDurationMs = completionDurationMs,
                ),
            )
            .build()
    }

    private fun buildMediaMetadata(
        surahName: String,
        reciterName: String,
        verseCount: Int,
        ayahNumber: Int,
        completionExtrasAyah: Int?,
        completionPositionMs: Long,
        completionDurationMs: Long,
    ): MediaMetadata {
        val metadataBuilder = MediaMetadata.Builder()
            .setTitle("$surahName • Ayah $ayahNumber")
            .setArtist(reciterName)
            .setAlbumTitle(surahName)
            .setTrackNumber(ayahNumber)
            .setTotalTrackCount(verseCount)
        if (ayahNumber == completionExtrasAyah) {
            metadataBuilder.setExtras(
                buildCompletionExtras(completionPositionMs, completionDurationMs),
            )
        }
        return metadataBuilder.build()
    }

    private fun buildCompletionExtras(positionMs: Long, durationMs: Long): Bundle {
        return Bundle().apply {
            when {
                durationMs > 0L && positionMs in 1 until durationMs -> {
                    putInt(
                        MediaConstants.EXTRAS_KEY_COMPLETION_STATUS,
                        MediaConstants.EXTRAS_VALUE_COMPLETION_STATUS_PARTIALLY_PLAYED,
                    )
                    putDouble(
                        MediaConstants.EXTRAS_KEY_COMPLETION_PERCENTAGE,
                        (positionMs.toDouble() / durationMs.toDouble()).coerceIn(0.0, 1.0),
                    )
                }
                durationMs > 0L && positionMs >= durationMs -> {
                    putInt(
                        MediaConstants.EXTRAS_KEY_COMPLETION_STATUS,
                        MediaConstants.EXTRAS_VALUE_COMPLETION_STATUS_FULLY_PLAYED,
                    )
                }
                else -> {
                    putInt(
                        MediaConstants.EXTRAS_KEY_COMPLETION_STATUS,
                        MediaConstants.EXTRAS_VALUE_COMPLETION_STATUS_NOT_PLAYED,
                    )
                }
            }
        }
    }

    fun mediaItemId(surahNumber: Int, ayahNumber: Int): String = "$surahNumber:$ayahNumber"

    fun ayahNumberFromMediaId(mediaId: String?): Int? {
        if (mediaId.isNullOrBlank()) return null
        return mediaId.substringAfter(':').toIntOrNull()
    }
}
