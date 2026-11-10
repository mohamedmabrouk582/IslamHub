package com.example.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "ayahs",
    primaryKeys = ["surahNumber", "ayahNumber"],
)
data class AyahEntity(
    val surahNumber: Int,
    val ayahNumber: Int,
    val textArabic: String,
    val textEnglish: String,
    val transliteration: String,
)
