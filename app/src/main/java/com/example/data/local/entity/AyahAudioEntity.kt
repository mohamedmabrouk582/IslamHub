package com.example.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "ayah_audio",
    primaryKeys = ["reciterId", "surahNumber", "ayahNumber"],
)
data class AyahAudioEntity(
    val reciterId: String,
    val surahNumber: Int,
    val ayahNumber: Int,
    val localPath: String,
    val remoteUrl: String,
    val fileSizeBytes: Long,
    val downloadedAt: Long,
)
