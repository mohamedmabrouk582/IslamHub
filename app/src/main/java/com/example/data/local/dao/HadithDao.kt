package com.example.data.local.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import com.example.data.local.entity.HadithBookEntity
import com.example.data.local.entity.HadithCollectionEntity
import com.example.data.local.entity.HadithEntity
import com.example.data.local.entity.HadithRemoteKey

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
interface HadithDao {
    @Query("SELECT COUNT(*) FROM hadith_collections")
    suspend fun countCollections(): Int

    @Query("SELECT * FROM hadith_collections ORDER BY nameEnglish ASC")
    fun pagingCollections(): PagingSource<Int, HadithCollectionEntity>

    @Query("SELECT * FROM hadith_collections ORDER BY nameEnglish ASC")
    suspend fun getCollections(): List<HadithCollectionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollections(collections: List<HadithCollectionEntity>)

    @Query("SELECT COUNT(*) FROM hadith_books WHERE collectionSlug = :slug")
    suspend fun countBooks(slug: String): Int

    @Query("SELECT * FROM hadith_books WHERE collectionSlug = :slug ORDER BY bookNumber ASC")
    fun pagingBooks(slug: String): PagingSource<Int, HadithBookEntity>

    @Query("SELECT * FROM hadith_books WHERE collectionSlug = :slug ORDER BY bookNumber ASC")
    suspend fun getBooks(slug: String): List<HadithBookEntity>

    @Query(
        "SELECT * FROM hadith_books WHERE collectionSlug = :slug AND bookNumber = :bookNumber LIMIT 1",
    )
    suspend fun getBook(slug: String, bookNumber: String): HadithBookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<HadithBookEntity>)

    @Query(
        "SELECT COUNT(*) FROM hadiths WHERE collectionSlug = :slug AND bookNumber = :bookNumber",
    )
    suspend fun countHadiths(slug: String, bookNumber: String): Int

    @Query(
        "SELECT * FROM hadiths WHERE collectionSlug = :slug AND bookNumber = :bookNumber ORDER BY id ASC",
    )
    fun pagingHadiths(slug: String, bookNumber: String): PagingSource<Int, HadithEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHadiths(hadiths: List<HadithEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRemoteKeys(keys: List<HadithRemoteKey>)

    @Query(
        "SELECT * FROM hadith_remote_keys WHERE collectionSlug = :slug AND bookNumber = :bookNumber ORDER BY nextKey DESC LIMIT 1",
    )
    suspend fun getLastRemoteKey(slug: String, bookNumber: String): HadithRemoteKey?

    @Query(
        "DELETE FROM hadiths WHERE collectionSlug = :slug AND bookNumber = :bookNumber",
    )
    suspend fun clearHadithsForBook(slug: String, bookNumber: String)

    @Query(
        "DELETE FROM hadith_remote_keys WHERE collectionSlug = :slug AND bookNumber = :bookNumber",
    )
    suspend fun clearRemoteKeysForBook(slug: String, bookNumber: String)

    @Transaction
    suspend fun replaceHadithPage(
        slug: String,
        bookNumber: String,
        hadiths: List<HadithEntity>,
        keys: List<HadithRemoteKey>,
        clearExisting: Boolean,
    ) {
        if (clearExisting) {
            clearHadithsForBook(slug, bookNumber)
            clearRemoteKeysForBook(slug, bookNumber)
        }
        insertHadiths(hadiths)
        insertRemoteKeys(keys)
    }
}
