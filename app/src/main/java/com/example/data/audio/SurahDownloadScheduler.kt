package com.example.data.audio

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.data.work.SurahAyahDownloadWorker
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class SurahDownloadScheduler @Inject constructor(
    private val workManager: WorkManager,
) {
    fun scheduleSurahDownload(
        reciterId: String,
        subfolder: String,
        surahNumber: Int,
        startAyah: Int,
        verseCount: Int,
    ) {
        val workName = workName(reciterId, surahNumber)
        val request = OneTimeWorkRequestBuilder<SurahAyahDownloadWorker>()
            .setInputData(
                workDataOf(
                    SurahAyahDownloadWorker.KEY_RECITER_ID to reciterId,
                    SurahAyahDownloadWorker.KEY_SUBFOLDER to subfolder,
                    SurahAyahDownloadWorker.KEY_SURAH_NUMBER to surahNumber,
                    SurahAyahDownloadWorker.KEY_START_AYAH to startAyah,
                    SurahAyahDownloadWorker.KEY_VERSE_COUNT to verseCount,
                ),
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build(),
            )
            .build()

        workManager.enqueueUniqueWork(workName, ExistingWorkPolicy.KEEP, request)
    }

    fun cancelSurahDownload(reciterId: String, surahNumber: Int) {
        workManager.cancelUniqueWork(workName(reciterId, surahNumber))
    }

    fun observeDownloadProgress(reciterId: String, surahNumber: Int): Flow<Float?> {
        val workName = workName(reciterId, surahNumber)
        return workManager.getWorkInfosForUniqueWorkFlow(workName).map { infos ->
            val activeInfo = infos.firstOrNull { info ->
                info.state == WorkInfo.State.RUNNING || info.state == WorkInfo.State.ENQUEUED
            } ?: return@map null

            val downloaded = activeInfo.progress.getInt(SurahAyahDownloadWorker.PROGRESS_DOWNLOADED, 0)
            val total = activeInfo.progress.getInt(SurahAyahDownloadWorker.PROGRESS_TOTAL, 0)
            if (total <= 0) {
                null
            } else {
                downloaded.toFloat() / total.toFloat()
            }
        }
    }

    fun isDownloading(reciterId: String, surahNumber: Int): Flow<Boolean> {
        val workName = workName(reciterId, surahNumber)
        return workManager.getWorkInfosForUniqueWorkFlow(workName).map { infos ->
            infos.any { info ->
                info.state == WorkInfo.State.RUNNING || info.state == WorkInfo.State.ENQUEUED
            }
        }
    }

    private fun workName(reciterId: String, surahNumber: Int): String {
        return "surah_download_${reciterId}_$surahNumber"
    }
}
