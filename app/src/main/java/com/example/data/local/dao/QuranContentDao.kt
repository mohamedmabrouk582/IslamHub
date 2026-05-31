package com.example.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.example.data.local.entity.AyahEntity
import com.example.data.local.entity.SurahEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SurahDao {
    @Query("SELECT * FROM surahs ORDER BY number ASC")
    fun observeAll(): Flow<List<SurahEntity>>

    @Query("SELECT * FROM surahs ORDER BY number ASC")
    suspend fun getAll(): List<SurahEntity>

    @Query("SELECT * FROM surahs WHERE number = :number LIMIT 1")
    suspend fun getByNumber(number: Int): SurahEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(surahs: List<SurahEntity>)

    @Query("SELECT COUNT(*) FROM surahs")
    suspend fun count(): Int
}

@Dao
interface AyahDao {
    @Query("SELECT * FROM ayahs WHERE surahNumber = :surahNumber ORDER BY ayahNumber ASC")
    suspend fun getBySurah(surahNumber: Int): List<AyahEntity>

    @Query("SELECT * FROM ayahs WHERE surahNumber = :surahNumber ORDER BY ayahNumber ASC")
    fun observeBySurah(surahNumber: Int): Flow<List<AyahEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ayahs: List<AyahEntity>)

    @Query("DELETE FROM ayahs WHERE surahNumber = :surahNumber")
    suspend fun deleteBySurah(surahNumber: Int)

    @Query("SELECT COUNT(*) FROM ayahs WHERE surahNumber = :surahNumber")
    suspend fun countBySurah(surahNumber: Int): Int
}
