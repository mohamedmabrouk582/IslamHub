package com.example.data

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey val id: String, // format "surah_num:ayah_num" or "surah_num" for surah level
    val surahNumber: Int,
    val ayahNumber: Int?, // Null for Surah, non-null for specific Ayah
    val surahName: String,
    val surahNameEnglish: String,
    val numberOfVerses: Int = 0,
    val arabicText: String = "",
    val translationText: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
