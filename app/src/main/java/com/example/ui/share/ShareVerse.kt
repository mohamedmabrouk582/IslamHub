package com.example.ui.share

import android.content.Context
import android.content.Intent
import com.example.data.model.Surah
import com.example.navigation.QuranDeepLinkParser

fun shareVerse(
    context: Context,
    surah: Surah,
    ayahNumber: Int,
    arabicSnippet: String? = null,
) {
    val reference = "${surah.number}:$ayahNumber"
    val appLink = QuranDeepLinkParser.buildHttpsUriString(surah.number, ayahNumber)
    val body = buildString {
        appendLine("${surah.nameEnglish} ($reference)")
        appendLine(surah.translation)
        if (!arabicSnippet.isNullOrBlank()) {
            appendLine()
            appendLine(arabicSnippet.trim())
        }
        appendLine()
        appendLine("Open in Al-Quran:")
        append(appLink)
    }
    val htmlBody = buildString {
        append("<p><b>${surah.nameEnglish}</b> ($reference)</p>")
        if (!arabicSnippet.isNullOrBlank()) {
            append("<p dir=\"rtl\">${arabicSnippet.trim()}</p>")
        }
        append("<p><a href=\"$appLink\">Open in Al-Quran</a></p>")
    }
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, body)
        putExtra(Intent.EXTRA_HTML_TEXT, htmlBody)
        putExtra(Intent.EXTRA_SUBJECT, "${surah.nameEnglish} $reference")
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share verse"))
}
