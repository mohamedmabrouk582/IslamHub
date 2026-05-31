package com.example.util

import androidx.core.text.HtmlCompat

object HtmlUtils {
    fun fromHtml(html: String): String {
        if (html.isBlank()) return html
        return HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT)
            .toString()
            .replace('\u00A0', ' ')
            .trim()
    }
}
