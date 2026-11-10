package com.example.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "tafseer",
    primaryKeys = ["tafseerId", "surahNumber", "ayahNumber"],
)
data class TafseerEntity(
    val tafseerId: Int,
    val surahNumber: Int,
    val ayahNumber: Int,
    val text: String,
    val tafseerName: String,
)
