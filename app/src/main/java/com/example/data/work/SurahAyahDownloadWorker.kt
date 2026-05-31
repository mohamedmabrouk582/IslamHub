package com.example.data.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.example.R
import com.example.data.repository.AyahAudioRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

@HiltWorker
class SurahAyahDownloadWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val ayahAudioRepository: AyahAudioRepository,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val reciterId = inputData.getString(KEY_RECITER_ID) ?: return Result.failure()
        val subfolder = inputData.getString(KEY_SUBFOLDER) ?: return Result.failure()
        val surahNumber = inputData.getInt(KEY_SURAH_NUMBER, 0)
        val startAyah = inputData.getInt(KEY_START_AYAH, 1)
        val verseCount = inputData.getInt(KEY_VERSE_COUNT, 0)

        if (surahNumber <= 0 || verseCount <= 0) {
            return Result.failure()
        }

        setForeground(createForegroundInfo(surahNumber, 0, verseCount))

        val downloadOrder = buildDownloadOrder(startAyah, verseCount)
        var downloadedCount = 0

        downloadOrder.chunked(CHUNK_SIZE).forEach { chunk ->
            coroutineScope {
                chunk.map { ayahNumber ->
                    async {
                        ayahAudioRepository.downloadAyah(
                            reciterId = reciterId,
                            subfolder = subfolder,
                            surahNumber = surahNumber,
                            ayahNumber = ayahNumber,
                        )
                    }
                }.awaitAll()
            }

            downloadedCount = ayahAudioRepository.getSurahDownloadProgress(
                reciterId = reciterId,
                surahNumber = surahNumber,
                verseCount = verseCount,
            ).let { progress ->
                (progress * verseCount).toInt().coerceAtMost(verseCount)
            }

            setProgressAsync(
                androidx.work.workDataOf(
                    PROGRESS_DOWNLOADED to downloadedCount,
                    PROGRESS_TOTAL to verseCount,
                ),
            )
            setForeground(createForegroundInfo(surahNumber, downloadedCount, verseCount))
        }

        return Result.success()
    }

    private fun createForegroundInfo(
        surahNumber: Int,
        downloaded: Int,
        total: Int,
    ): ForegroundInfo {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(appContext, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(appContext.getString(R.string.downloading_surah_notification_title, surahNumber))
            .setContentText(
                appContext.getString(
                    R.string.downloading_surah_notification_text,
                    downloaded,
                    total,
                ),
            )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(total, downloaded, total == 0)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
            )
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = appContext.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            appContext.getString(R.string.download_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        )
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val KEY_RECITER_ID = "reciter_id"
        const val KEY_SUBFOLDER = "subfolder"
        const val KEY_SURAH_NUMBER = "surah_number"
        const val KEY_START_AYAH = "start_ayah"
        const val KEY_VERSE_COUNT = "verse_count"

        const val PROGRESS_DOWNLOADED = "progress_downloaded"
        const val PROGRESS_TOTAL = "progress_total"

        private const val CHUNK_SIZE = 3
        private const val NOTIFICATION_CHANNEL_ID = "surah_audio_download"
        private const val NOTIFICATION_ID = 2001

        fun buildDownloadOrder(startAyah: Int, verseCount: Int): List<Int> {
            val result = mutableListOf<Int>()
            var offset = 0
            while (result.size < verseCount) {
                if (offset == 0) {
                    if (startAyah in 1..verseCount) {
                        result.add(startAyah)
                    }
                } else {
                    val higher = startAyah + offset
                    val lower = startAyah - offset
                    if (higher <= verseCount) {
                        result.add(higher)
                    }
                    if (lower >= 1) {
                        result.add(lower)
                    }
                }
                offset++
            }
            return result
        }
    }
}
