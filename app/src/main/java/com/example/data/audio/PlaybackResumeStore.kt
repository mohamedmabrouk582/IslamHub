package com.example.data.audio

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class PlaybackSnapshot(
    val reciterId: String,
    val subfolder: String,
    val surahNumber: Int,
    val ayahNumber: Int,
    val verseCount: Int,
    val surahName: String,
    val reciterName: String,
    val mediaItemIndex: Int,
    val positionMs: Long,
    val durationMs: Long,
    val playbackSpeed: Float,
)

@Singleton
class PlaybackResumeStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun save(snapshot: PlaybackSnapshot) {
        prefs.edit()
            .putString(KEY_RECITER_ID, snapshot.reciterId)
            .putString(KEY_SUBFOLDER, snapshot.subfolder)
            .putInt(KEY_SURAH_NUMBER, snapshot.surahNumber)
            .putInt(KEY_AYAH_NUMBER, snapshot.ayahNumber)
            .putInt(KEY_VERSE_COUNT, snapshot.verseCount)
            .putString(KEY_SURAH_NAME, snapshot.surahName)
            .putString(KEY_RECITER_NAME, snapshot.reciterName)
            .putInt(KEY_MEDIA_ITEM_INDEX, snapshot.mediaItemIndex)
            .putLong(KEY_POSITION_MS, snapshot.positionMs)
            .putLong(KEY_DURATION_MS, snapshot.durationMs)
            .putFloat(KEY_PLAYBACK_SPEED, snapshot.playbackSpeed)
            .putBoolean(KEY_HAS_SNAPSHOT, true)
            .apply()
    }

    fun load(): PlaybackSnapshot? {
        if (!prefs.getBoolean(KEY_HAS_SNAPSHOT, false)) return null
        val reciterId = prefs.getString(KEY_RECITER_ID, null) ?: return null
        val subfolder = prefs.getString(KEY_SUBFOLDER, null) ?: return null
        val surahNumber = prefs.getInt(KEY_SURAH_NUMBER, 0)
        val ayahNumber = prefs.getInt(KEY_AYAH_NUMBER, 0)
        val verseCount = prefs.getInt(KEY_VERSE_COUNT, 0)
        val surahName = prefs.getString(KEY_SURAH_NAME, null) ?: return null
        val reciterName = prefs.getString(KEY_RECITER_NAME, null) ?: return null
        if (surahNumber !in 1..114 || ayahNumber < 1 || verseCount < 1) return null

        return PlaybackSnapshot(
            reciterId = reciterId,
            subfolder = subfolder,
            surahNumber = surahNumber,
            ayahNumber = ayahNumber,
            verseCount = verseCount,
            surahName = surahName,
            reciterName = reciterName,
            mediaItemIndex = prefs.getInt(KEY_MEDIA_ITEM_INDEX, (ayahNumber - 1).coerceAtLeast(0)),
            positionMs = prefs.getLong(KEY_POSITION_MS, 0L),
            durationMs = prefs.getLong(KEY_DURATION_MS, 0L),
            playbackSpeed = prefs.getFloat(KEY_PLAYBACK_SPEED, 1f),
        )
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "quran_playback_resume"
        private const val KEY_HAS_SNAPSHOT = "has_snapshot"
        private const val KEY_RECITER_ID = "reciter_id"
        private const val KEY_SUBFOLDER = "subfolder"
        private const val KEY_SURAH_NUMBER = "surah_number"
        private const val KEY_AYAH_NUMBER = "ayah_number"
        private const val KEY_VERSE_COUNT = "verse_count"
        private const val KEY_SURAH_NAME = "surah_name"
        private const val KEY_RECITER_NAME = "reciter_name"
        private const val KEY_MEDIA_ITEM_INDEX = "media_item_index"
        private const val KEY_POSITION_MS = "position_ms"
        private const val KEY_DURATION_MS = "duration_ms"
        private const val KEY_PLAYBACK_SPEED = "playback_speed"
    }
}
