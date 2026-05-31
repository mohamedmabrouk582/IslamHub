package com.example.data.paging.hadith

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.example.data.local.dao.HadithDao
import com.example.data.mapper.toEntity
import com.example.data.remote.sunnah.SunnahApi

@OptIn(ExperimentalPagingApi::class)
class HadithCollectionRemoteMediator(
    private val sunnahApi: SunnahApi,
    private val hadithDao: HadithDao,
) : RemoteMediator<Int, com.example.data.local.entity.HadithCollectionEntity>() {

    override suspend fun initialize(): InitializeAction {
        return if (hadithDao.countCollections() > 0) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, com.example.data.local.entity.HadithCollectionEntity>,
    ): MediatorResult {
        if (loadType == LoadType.APPEND || loadType == LoadType.PREPEND) {
            return MediatorResult.Success(endOfPaginationReached = true)
        }
        return try {
            val remote = sunnahApi.getCollections().data.map { it.toEntity() }
            hadithDao.insertCollections(remote)
            MediatorResult.Success(endOfPaginationReached = true)
        } catch (error: Exception) {
            if (hadithDao.countCollections() > 0) {
                MediatorResult.Success(endOfPaginationReached = true)
            } else {
                MediatorResult.Error(error)
            }
        }
    }
}

@OptIn(ExperimentalPagingApi::class)
class HadithBookRemoteMediator(
    private val collectionSlug: String,
    private val sunnahApi: SunnahApi,
    private val hadithDao: HadithDao,
) : RemoteMediator<Int, com.example.data.local.entity.HadithBookEntity>() {

    override suspend fun initialize(): InitializeAction {
        return if (hadithDao.countBooks(collectionSlug) > 0) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, com.example.data.local.entity.HadithBookEntity>,
    ): MediatorResult {
        if (loadType == LoadType.APPEND || loadType == LoadType.PREPEND) {
            return MediatorResult.Success(endOfPaginationReached = true)
        }
        return try {
            val remote = sunnahApi.getBooks(collectionSlug).data.map { it.toEntity(collectionSlug) }
            hadithDao.insertBooks(remote)
            MediatorResult.Success(endOfPaginationReached = true)
        } catch (error: Exception) {
            if (hadithDao.countBooks(collectionSlug) > 0) {
                MediatorResult.Success(endOfPaginationReached = true)
            } else {
                MediatorResult.Error(error)
            }
        }
    }
}

@OptIn(ExperimentalPagingApi::class)
class HadithListRemoteMediator(
    private val collectionSlug: String,
    private val bookNumber: String,
    private val sunnahApi: SunnahApi,
    private val hadithDao: HadithDao,
) : RemoteMediator<Int, com.example.data.local.entity.HadithEntity>() {

    override suspend fun initialize(): InitializeAction {
        val book = hadithDao.getBook(collectionSlug, bookNumber) ?: return InitializeAction.LAUNCH_INITIAL_REFRESH
        val cachedCount = hadithDao.countHadiths(collectionSlug, bookNumber)
        return if (cachedCount >= book.hadithCount && book.hadithCount > 0) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, com.example.data.local.entity.HadithEntity>,
    ): MediatorResult {
        return try {
            val page = when (loadType) {
                LoadType.REFRESH -> 1
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val nextKey = hadithDao.getLastRemoteKey(collectionSlug, bookNumber)?.nextKey
                    nextKey ?: return MediatorResult.Success(endOfPaginationReached = true)
                }
            }

            if (loadType == LoadType.REFRESH) {
                hadithDao.clearHadithsForBook(collectionSlug, bookNumber)
                hadithDao.clearRemoteKeysForBook(collectionSlug, bookNumber)
            }

            val collectionName = hadithDao.getCollections()
                .firstOrNull { it.slug == collectionSlug }
                ?.nameEnglish
                .orEmpty()
            val bookName = hadithDao.getBook(collectionSlug, bookNumber)?.nameEnglish.orEmpty()

            val response = sunnahApi.getBookHadiths(collectionSlug, bookNumber, page = page)
            val entities = response.data.map { dto ->
                dto.toEntity(
                    collectionSlug = collectionSlug,
                    bookNumber = bookNumber,
                    collectionName = collectionName,
                    bookName = bookName,
                )
            }
            val remoteKeys = entities.map { entity ->
                com.example.data.local.entity.HadithRemoteKey(
                    hadithId = entity.id,
                    collectionSlug = collectionSlug,
                    bookNumber = bookNumber,
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = response.next,
                )
            }

            hadithDao.replaceHadithPage(
                slug = collectionSlug,
                bookNumber = bookNumber,
                hadiths = entities,
                keys = remoteKeys,
                clearExisting = false,
            )

            val endReached = response.next == null ||
                (response.total != null && response.limit != null &&
                    page * response.limit >= response.total)

            MediatorResult.Success(endOfPaginationReached = endReached)
        } catch (error: Exception) {
            if (hadithDao.countHadiths(collectionSlug, bookNumber) > 0) {
                MediatorResult.Success(endOfPaginationReached = true)
            } else {
                MediatorResult.Error(error)
            }
        }
    }
}
