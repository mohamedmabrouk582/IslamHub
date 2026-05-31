package com.example.data.local.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "hadith_remote_keys")
data class HadithRemoteKey(
    @PrimaryKey val hadithId: String,
    val collectionSlug: String,
    val bookNumber: String,
    val prevKey: Int?,
    val nextKey: Int?,
)
