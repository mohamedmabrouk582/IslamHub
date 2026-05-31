package com.example.data.remote.tafseer

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path

interface TafseerApi {
    @GET("tafseer/{tafseerId}/{surah}/{ayah}")
    suspend fun getTafseer(
        @Path("tafseerId") tafseerId: Int,
        @Path("surah") surah: Int,
        @Path("ayah") ayah: Int,
    ): TafseerDto
}

@JsonClass(generateAdapter = true)
data class TafseerDto(
    @Json(name = "tafseer_id") val tafseerId: Int,
    @Json(name = "tafseer_name") val tafseerName: String,
    @Json(name = "ayah_number") val ayahNumber: Int,
    val text: String,
)
