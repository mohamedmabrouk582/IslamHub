package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "last_read")
data class LastRead(
    @PrimaryKey val id: Int = 1, // Singleton row
    val surahNumber: Int,
    val ayahNumber: Int,
    val surahName: String,
    val surahNameEnglish: String,
    val timestamp: Long = System.currentTimeMillis()
)
