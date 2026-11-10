package com.example.data.repository

import com.example.data.QuranData
import com.example.data.local.dao.AyahDao
import com.example.data.local.dao.SurahDao
import com.example.data.local.entity.AyahEntity
import com.example.data.mapper.toDomain
import com.example.data.mapper.toEntity
import com.example.data.model.Ayah
import com.example.data.model.Surah
import com.example.data.remote.quran.QuranApi
import com.example.ui.common.UiState
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Singleton
class QuranRepository @Inject constructor(
    private val quranApi: QuranApi,
    private val surahDao: SurahDao,
    private val ayahDao: AyahDao,
) {
    fun getSurahs(): Flow<UiState<List<Surah>>> = flow {
        emit(UiState.Loading)
        val cached = surahDao.getAll().map { it.toDomain() }
        if (surahDao.count() == TOTAL_SURAHS) {
            emit(UiState.Success(cached))
            return@flow
        }
        try {
            val remote = quranApi.getChapters().chapters.map { it.toEntity() }
            surahDao.insertAll(remote)
            emit(UiState.Success(remote.map { it.toDomain() }))
        } catch (error: Exception) {
            if (cached.isNotEmpty()) {
                emit(UiState.Success(cached))
            } else {
                val offlineFallback = QuranData.surahs.map { it.toDomainModel() }
                if (offlineFallback.isNotEmpty()) {
                    emit(UiState.Success(offlineFallback))
                } else {
                    emit(UiState.Error(error.message ?: "Failed to load surahs"))
                }
            }
        }
    }

    suspend fun getSurah(number: Int): Surah? {
        return surahDao.getByNumber(number)?.toDomain()
            ?: QuranData.surahs.find { it.number == number }?.toDomainModel()
    }

    fun getAyahs(surahNumber: Int, translationId: Int = 21): Flow<UiState<List<Ayah>>> = flow {
        val expectedCount = surahDao.getByNumber(surahNumber)?.versesCount
        val cached = ayahDao.getBySurah(surahNumber).map { it.toDomain() }
        if (expectedCount != null && ayahDao.countBySurah(surahNumber) == expectedCount) {
            emit(UiState.Success(cached))
            return@flow
        }
        emit(UiState.Loading)
        try {
            val remote = fetchAllVerses(surahNumber, translationId, expectedCount)
            ayahDao.deleteBySurah(surahNumber)
            ayahDao.insertAll(remote)
            emit(UiState.Success(remote.map { it.toDomain() }))
        } catch (error: Exception) {
            if (cached.isNotEmpty()) {
                emit(UiState.Success(cached))
            } else {
                val offlineFallback = QuranData.getAyahsForSurah(surahNumber).map {
                    Ayah(
                        number = it.number,
                        surahNumber = it.surahNumber,
                        textArabic = it.textArabic,
                        textEnglish = it.textEnglish,
                        transliteration = it.transliteration,
                    )
                }
                if (offlineFallback.isNotEmpty()) {
                    emit(UiState.Success(offlineFallback))
                } else {
                    emit(UiState.Error(error.message ?: "Failed to load ayahs"))
                }
            }
        }
    }

    private suspend fun fetchAllVerses(
        surahNumber: Int,
        translationId: Int,
        expectedCount: Int?,
    ): List<AyahEntity> {
        if (expectedCount != null) {
            val response = quranApi.getChapterVerses(
                chapterId = surahNumber,
                translations = translationId,
                perPage = expectedCount,
            )
            if (response.verses.size >= expectedCount) {
                return response.verses.map { it.toEntity() }
            }
        }

        val allVerses = mutableListOf<AyahEntity>()
        var page = 1
        var totalPages = 1
        do {
            val response = quranApi.getChapterVerses(
                chapterId = surahNumber,
                translations = translationId,
                page = page,
            )
            allVerses.addAll(response.verses.map { it.toEntity() })
            totalPages = response.pagination?.totalPages ?: 1
            page++
        } while (page <= totalPages)
        return allVerses
    }

    private fun com.example.data.Surah.toDomainModel(): Surah = Surah(
        number = number,
        nameArabic = nameArabic,
        nameEnglish = nameEnglish,
        translation = translation,
        type = type,
        versesCount = versesCount,
        juzNumber = juzNumber,
        hasSajdah = hasSajdah,
    )

    private companion object {
        const val TOTAL_SURAHS = 114
    }
}
