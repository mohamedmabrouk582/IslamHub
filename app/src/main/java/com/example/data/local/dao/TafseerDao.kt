package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.TafseerEntity

@Dao
interface TafseerDao {
    @Query(
        "SELECT * FROM tafseer WHERE tafseerId = :tafseerId AND surahNumber = :surah AND ayahNumber = :ayah LIMIT 1",
    )
    suspend fun get(tafseerId: Int, surah: Int, ayah: Int): TafseerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TafseerEntity)
}
