package com.example.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "hadith_books",
    primaryKeys = ["collectionSlug", "bookNumber"],
)
data class HadithBookEntity(
    val collectionSlug: String,
    val bookNumber: String,
    val nameEnglish: String,
    val nameArabic: String,
    val hadithCount: Int,
)
