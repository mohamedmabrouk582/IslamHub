package com.example.data.local.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "hadiths")
data class HadithEntity(
    @PrimaryKey val id: String,
    val collectionSlug: String,
    val bookNumber: String,
    val textArabic: String,
    val textEnglish: String,
    val narrator: String,
    val collectionName: String,
    val bookName: String,
)
