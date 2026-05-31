package com.example.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.data.local.dao.HadithDao
import com.example.data.mapper.toDomain
import com.example.data.model.Hadith
import com.example.data.model.HadithBook
import com.example.data.model.HadithCollection
import com.example.data.paging.hadith.HadithBookRemoteMediator
import com.example.data.paging.hadith.HadithCollectionRemoteMediator
import com.example.data.paging.hadith.HadithListRemoteMediator
import com.example.data.remote.sunnah.SunnahApi
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class HadithRepository @Inject constructor(
    private val sunnahApi: SunnahApi,
    private val hadithDao: HadithDao,
) {
    @OptIn(ExperimentalPagingApi::class)
    fun collectionsPaging(): Flow<PagingData<HadithCollection>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
            remoteMediator = HadithCollectionRemoteMediator(sunnahApi, hadithDao),
            pagingSourceFactory = { hadithDao.pagingCollections() },
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    @OptIn(ExperimentalPagingApi::class)
    fun booksPaging(collectionSlug: String): Flow<PagingData<HadithBook>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
            remoteMediator = HadithBookRemoteMediator(collectionSlug, sunnahApi, hadithDao),
            pagingSourceFactory = { hadithDao.pagingBooks(collectionSlug) },
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    @OptIn(ExperimentalPagingApi::class)
    fun hadithsPaging(collectionSlug: String, bookNumber: String): Flow<PagingData<Hadith>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
            remoteMediator = HadithListRemoteMediator(collectionSlug, bookNumber, sunnahApi, hadithDao),
            pagingSourceFactory = { hadithDao.pagingHadiths(collectionSlug, bookNumber) },
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    suspend fun getCollectionName(slug: String): String? {
        return hadithDao.getCollections().firstOrNull { it.slug == slug }?.nameEnglish
    }

    suspend fun getBookName(collectionSlug: String, bookNumber: String): String? {
        return hadithDao.getBook(collectionSlug, bookNumber)?.nameEnglish
    }

    private companion object {
        const val PAGE_SIZE = 20
    }
}
