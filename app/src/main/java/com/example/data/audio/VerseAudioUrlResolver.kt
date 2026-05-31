package com.example.data.audio

object VerseAudioUrlResolver {
    private const val BASE_URL = "https://www.everyayah.com/data"

    fun ayahUrl(
        subfolder: String,
        surah: Int,
        ayah: Int,
    ): String {
        val surahPart = surah.toString().padStart(3, '0')
        val ayahPart = ayah.toString().padStart(3, '0')
        return "$BASE_URL/$subfolder/${surahPart}${ayahPart}.mp3"
    }

    fun ayahFileName(surah: Int, ayah: Int): String {
        val surahPart = surah.toString().padStart(3, '0')
        val ayahPart = ayah.toString().padStart(3, '0')
        return "${surahPart}${ayahPart}.mp3"
    }
}
