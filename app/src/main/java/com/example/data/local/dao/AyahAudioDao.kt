package com.example.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.example.data.local.entity.AyahAudioEntity

@Dao
interface AyahAudioDao {
    @Query(
        """
        SELECT * FROM ayah_audio
        WHERE reciterId = :reciterId
          AND surahNumber = :surahNumber
          AND ayahNumber = :ayahNumber
        LIMIT 1
        """,
    )
    suspend fun getAyah(
        reciterId: String,
        surahNumber: Int,
        ayahNumber: Int,
    ): AyahAudioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AyahAudioEntity)

    @Query(
        """
        SELECT COUNT(*) FROM ayah_audio
        WHERE reciterId = :reciterId AND surahNumber = :surahNumber
        """,
    )
    suspend fun countDownloadedAyahs(reciterId: String, surahNumber: Int): Int
}
