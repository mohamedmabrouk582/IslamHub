package com.example.data.model

import androidx.annotation.DrawableRes

data class Surah(
    val number: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val translation: String,
    val type: String,
    val versesCount: Int,
    val juzNumber: Int = 1,
    val hasSajdah: Boolean = false,
)

data class Ayah(
    val number: Int,
    val surahNumber: Int,
    val textArabic: String,
    val textEnglish: String,
    val transliteration: String = "",
)

data class Reciter(
    val id: String,
    val everyAyahId: Int,
    val subfolder: String,
    val nameEnglish: String,
    val nameArabic: String? = null,
    val bitrateLabel: String,
    @DrawableRes val avatarResId: Int,
)

data class Hadith(
    val id: String,
    val textArabic: String,
    val textEnglish: String,
    val narrator: String,
    val collection: String,
    val bookName: String = "",
)

data class HadithCollection(
    val slug: String,
    val nameEnglish: String,
    val nameArabic: String,
    val totalHadith: Int,
)

data class HadithBook(
    val collectionSlug: String,
    val bookNumber: String,
    val nameEnglish: String,
    val nameArabic: String,
    val hadithCount: Int,
)
