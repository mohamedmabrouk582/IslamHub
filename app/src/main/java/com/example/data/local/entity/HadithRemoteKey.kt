package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hadith_remote_keys")
data class HadithRemoteKey(
    @PrimaryKey val hadithId: String,
    val collectionSlug: String,
    val bookNumber: String,
    val prevKey: Int?,
    val nextKey: Int?,
)
