package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hadith_collections")
data class HadithCollectionEntity(
    @PrimaryKey val slug: String,
    val nameEnglish: String,
    val nameArabic: String,
    val totalHadith: Int,
)
