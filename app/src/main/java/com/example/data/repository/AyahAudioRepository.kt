package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.audio.VerseAudioUrlResolver
import com.example.data.local.dao.AyahAudioDao
import com.example.data.local.entity.AyahAudioEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

@Singleton
class AyahAudioRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ayahAudioDao: AyahAudioDao,
    @Named("audio") private val okHttpClient: OkHttpClient,
) {
    suspend fun resolvePlaybackUri(
        reciterId: String,
        subfolder: String,
        surahNumber: Int,
        ayahNumber: Int,
    ): Uri {
        val cached = ayahAudioDao.getAyah(reciterId, surahNumber, ayahNumber)
        if (cached != null) {
            val file = File(cached.localPath)
            if (file.exists()) {
                return Uri.fromFile(file)
            }
        }
        val remoteUrl = VerseAudioUrlResolver.ayahUrl(subfolder, surahNumber, ayahNumber)
        return Uri.parse(remoteUrl)
    }

    suspend fun isAyahDownloaded(
        reciterId: String,
        surahNumber: Int,
        ayahNumber: Int,
    ): Boolean {
        val cached = ayahAudioDao.getAyah(reciterId, surahNumber, ayahNumber) ?: return false
        return File(cached.localPath).exists()
    }

    suspend fun getSurahDownloadProgress(
        reciterId: String,
        surahNumber: Int,
        verseCount: Int,
    ): Float {
        if (verseCount <= 0) return 0f
        val downloaded = ayahAudioDao.countDownloadedAyahs(reciterId, surahNumber)
        return downloaded.toFloat() / verseCount.toFloat()
    }

    suspend fun downloadAyah(
        reciterId: String,
        subfolder: String,
        surahNumber: Int,
        ayahNumber: Int,
    ): Boolean = withContext(Dispatchers.IO) {
        if (isAyahDownloaded(reciterId, surahNumber, ayahNumber)) {
            return@withContext true
        }

        val remoteUrl = VerseAudioUrlResolver.ayahUrl(subfolder, surahNumber, ayahNumber)
        val targetFile = localAudioFile(reciterId, surahNumber, ayahNumber)
        targetFile.parentFile?.mkdirs()

        val request = Request.Builder().url(remoteUrl).build()
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return@withContext false
            }
            val body = response.body ?: return@withContext false
            targetFile.outputStream().use { output ->
                body.byteStream().copyTo(output)
            }
        }

        ayahAudioDao.insert(
            AyahAudioEntity(
                reciterId = reciterId,
                surahNumber = surahNumber,
                ayahNumber = ayahNumber,
                localPath = targetFile.absolutePath,
                remoteUrl = remoteUrl,
                fileSizeBytes = targetFile.length(),
                downloadedAt = System.currentTimeMillis(),
            ),
        )
        true
    }

    fun localAudioFile(
        reciterId: String,
        surahNumber: Int,
        ayahNumber: Int,
    ): File {
        val fileName = VerseAudioUrlResolver.ayahFileName(surahNumber, ayahNumber)
        return File(context.filesDir, "audio/$reciterId/$fileName")
    }
}
