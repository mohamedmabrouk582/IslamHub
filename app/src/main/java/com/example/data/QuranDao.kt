package com.example.data

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranDao {
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<Bookmark>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: Bookmark)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmark(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE id = :id)")
    suspend fun isBookmarked(id: String): Boolean

    @Query("SELECT * FROM last_read WHERE id = 1 LIMIT 1")
    fun getLastRead(): Flow<LastRead?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateLastRead(lastRead: LastRead)
}
