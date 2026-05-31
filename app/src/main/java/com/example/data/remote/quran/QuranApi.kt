package com.example.data.remote.quran

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface QuranApi {
    @GET("chapters")
    suspend fun getChapters(): ChaptersResponse

    @GET("chapters/{chapterId}/verses")
    suspend fun getChapterVerses(
        @Path("chapterId") chapterId: Int,
        @Query("recitation") recitation: Int = 1,
        @Query("translations") translations: Int = 21,
        @Query("language") language: String = "en",
        @Query("text_type") textType: String = "words",
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null,
    ): VersesResponse
}

@JsonClass(generateAdapter = true)
data class ChaptersResponse(val chapters: List<ChapterDto>)

@JsonClass(generateAdapter = true)
data class ChapterDto(
    @Json(name = "chapter_number") val chapterNumber: Int,
    @Json(name = "name_arabic") val nameArabic: String,
    @Json(name = "name_simple") val nameSimple: String,
    @Json(name = "revelation_place") val revelationPlace: String,
    @Json(name = "verses_count") val versesCount: Int,
    @Json(name = "translated_name") val translatedName: TranslatedNameDto,
)

@JsonClass(generateAdapter = true)
data class TranslatedNameDto(val name: String)

@JsonClass(generateAdapter = true)
data class VersesResponse(
    val verses: List<VerseDto>,
    val pagination: PaginationDto? = null,
)

@JsonClass(generateAdapter = true)
data class PaginationDto(
    @Json(name = "current_page") val currentPage: Int,
    @Json(name = "next_page") val nextPage: Int?,
    @Json(name = "prev_page") val prevPage: Int?,
    @Json(name = "total_pages") val totalPages: Int,
    @Json(name = "total_count") val totalCount: Int,
)

@JsonClass(generateAdapter = true)
data class VerseDto(
    @Json(name = "verse_number") val verseNumber: Int,
    @Json(name = "chapter_id") val chapterId: Int,
    @Json(name = "text_madani") val textMadani: String?,
    val words: List<WordDto>?,
    val translations: List<VerseTranslationDto>?,
)

@JsonClass(generateAdapter = true)
data class WordDto(
    @Json(name = "char_type") val charType: String?,
    val transliteration: WordTransliterationDto?,
)

@JsonClass(generateAdapter = true)
data class WordTransliterationDto(val text: String?)

@JsonClass(generateAdapter = true)
data class VerseTranslationDto(val text: String)
