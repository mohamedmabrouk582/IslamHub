package com.example.data.repository

import com.example.data.local.dao.TafseerDao
import com.example.data.mapper.TafseerSourceMapper
import com.example.data.mapper.toEntity
import com.example.data.remote.tafseer.TafseerApi
import com.example.ui.common.UiState
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Singleton
class TafseerRepository @Inject constructor(
    private val tafseerApi: TafseerApi,
    private val tafseerDao: TafseerDao,
) {
    fun getTafseer(
        sourceLabel: String,
        surahNumber: Int,
        ayahNumber: Int,
    ): Flow<UiState<String>> = flow {
        emit(UiState.Loading)
        val tafseerId = TafseerSourceMapper.toTafseerId(sourceLabel)
        val cached = tafseerDao.get(tafseerId, surahNumber, ayahNumber)?.text
        if (!cached.isNullOrBlank()) {
            emit(UiState.Success(cached))
        }
        try {
            val remote = tafseerApi.getTafseer(tafseerId, surahNumber, ayahNumber)
            tafseerDao.insert(remote.toEntity(surahNumber))
            emit(UiState.Success(remote.text))
        } catch (error: Exception) {
            if (cached.isNullOrBlank()) {
                emit(UiState.Error(error.message ?: "Failed to load tafseer"))
            }
        }
    }
}
