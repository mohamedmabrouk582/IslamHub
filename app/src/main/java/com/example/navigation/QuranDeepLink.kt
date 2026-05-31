package com.example.navigation

import android.net.Uri
import com.example.BuildConfig

data class QuranDeepLink(
    val surahNumber: Int,
    val ayahNumber: Int,
)

object QuranDeepLinkParser {
    private const val SCHEME = "islamhub"
    private const val HOST = "quran"
    private const val QURAN_SEGMENT = "quran"

    val httpsHost: String get() = BuildConfig.DEEP_LINK_HOST
    private val httpsPathPrefix: String get() = BuildConfig.DEEP_LINK_PATH_PREFIX

    fun buildUri(surahNumber: Int, ayahNumber: Int): Uri {
        return Uri.parse(buildUriString(surahNumber, ayahNumber))
    }

    fun buildUriString(surahNumber: Int, ayahNumber: Int): String {
        return "$SCHEME://$HOST/$surahNumber/$ayahNumber"
    }

    /** Clickable https link for WhatsApp, SMS, Notes, etc. */
    fun buildHttpsUriString(surahNumber: Int, ayahNumber: Int): String {
        return "https://$httpsHost/$httpsPathPrefix/$surahNumber/$ayahNumber"
    }

    /**
     * Parses custom-scheme and https app links:
     * - islamhub://quran/2/255
     * - https://mohamedmabrouk582.github.io/IslamHub/quran/2/255
     */
    fun parseUri(uri: Uri?, maxAyahInSurah: Int? = null): QuranDeepLink? {
        if (uri == null) return null

        val parsed = when (uri.scheme?.lowercase()) {
            SCHEME -> parseCustomScheme(uri)
            "http", "https" -> parseHttpsScheme(uri)
            else -> null
        } ?: return null

        if (parsed.surahNumber !in 1..114 || parsed.ayahNumber < 1) return null
        if (maxAyahInSurah != null && parsed.ayahNumber > maxAyahInSurah) return null
        return parsed
    }

    private fun parseCustomScheme(uri: Uri): QuranDeepLink? {
        if (uri.host != HOST) return null
        val segments = uri.pathSegments
        if (segments.size < 2) return null
        val surahNumber = segments[0].toIntOrNull() ?: return null
        val ayahNumber = segments[1].toIntOrNull() ?: return null
        return QuranDeepLink(surahNumber = surahNumber, ayahNumber = ayahNumber)
    }

    private fun parseHttpsScheme(uri: Uri): QuranDeepLink? {
        if (!uri.host.equals(httpsHost, ignoreCase = true)) return null
        val segments = uri.pathSegments.filter { it.isNotBlank() }
        val quranIndex = segments.indexOf(QURAN_SEGMENT)
        if (quranIndex < 0 || segments.size < quranIndex + 3) return null
        val surahNumber = segments[quranIndex + 1].toIntOrNull() ?: return null
        val ayahNumber = segments[quranIndex + 2].toIntOrNull() ?: return null
        return QuranDeepLink(surahNumber = surahNumber, ayahNumber = ayahNumber)
    }
}
