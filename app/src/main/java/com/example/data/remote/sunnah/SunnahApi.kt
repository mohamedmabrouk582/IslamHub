package com.example.data.remote.sunnah

import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SunnahApi {
    @GET("v1/collections")
    suspend fun getCollections(): SunnahCollectionsResponse

    @GET("v1/collections/{collectionName}/books")
    suspend fun getBooks(
        @Path("collectionName") collectionName: String,
        @Query("limit") limit: Int = 150,
    ): SunnahBooksResponse

    @GET("v1/collections/{collectionName}/books/{bookNumber}/hadiths")
    suspend fun getBookHadiths(
        @Path("collectionName") collectionName: String,
        @Path("bookNumber") bookNumber: String,
        @Query("paginated") paginated: Int = 1,
        @Query("page") page: Int = 1,
    ): SunnahHadithsResponse
}

@JsonClass(generateAdapter = true)
data class SunnahCollectionsResponse(val data: List<SunnahCollectionDto>)

@JsonClass(generateAdapter = true)
data class SunnahCollectionDto(
    val name: String,
    @com.squareup.moshi.Json(name = "totalHadith") val totalHadith: Int,
    val collection: List<SunnahLocalizedTextDto>,
)

@JsonClass(generateAdapter = true)
data class SunnahBooksResponse(val data: List<SunnahBookDto>)

@JsonClass(generateAdapter = true)
data class SunnahBookDto(
    @com.squareup.moshi.Json(name = "bookNumber") val bookNumber: String,
    val book: List<SunnahBookTextDto>,
    @com.squareup.moshi.Json(name = "numberOfHadith") val numberOfHadith: Int,
)

@JsonClass(generateAdapter = true)
data class SunnahBookTextDto(
    val lang: String,
    val name: String,
)

@JsonClass(generateAdapter = true)
data class SunnahHadithsResponse(
    val data: List<SunnahHadithDto>,
    val total: Int? = null,
    val limit: Int? = null,
    val next: Int? = null,
)

@JsonClass(generateAdapter = true)
data class SunnahHadithDto(
    @com.squareup.moshi.Json(name = "hadithNumber") val hadithNumber: String,
    val hadith: List<SunnahHadithTextDto>,
)

@JsonClass(generateAdapter = true)
data class SunnahHadithTextDto(
    val lang: String,
    val body: String,
    @com.squareup.moshi.Json(name = "chapterTitle") val chapterTitle: String? = null,
)

@JsonClass(generateAdapter = true)
data class SunnahLocalizedTextDto(
    val lang: String,
    val title: String,
)
