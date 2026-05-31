package com.example.data.local.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "surahs")
data class SurahEntity(
    @PrimaryKey val number: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val translation: String,
    val type: String,
    val versesCount: Int,
    val juzNumber: Int = 1,
    val hasSajdah: Boolean = false,
)
