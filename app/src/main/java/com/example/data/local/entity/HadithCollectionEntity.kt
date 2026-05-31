package com.example.data.local.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "hadith_collections")
data class HadithCollectionEntity(
    @PrimaryKey val slug: String,
    val nameEnglish: String,
    val nameArabic: String,
    val totalHadith: Int,
)
