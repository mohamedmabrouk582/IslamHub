package com.example.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppNavKey : NavKey

@Serializable
sealed interface TopLevelNavKey : AppNavKey

@Serializable
data object HomeKey : TopLevelNavKey

@Serializable
data object QuranKey : TopLevelNavKey

@Serializable
data object BookmarksKey : TopLevelNavKey

@Serializable
data object SettingsKey : TopLevelNavKey

@Serializable
data object HadithRootKey : TopLevelNavKey

@Serializable
data object StoriesKey : TopLevelNavKey

@Serializable
data class HadithBooksKey(val slug: String) : AppNavKey

@Serializable
data class HadithListKey(val slug: String, val bookNumber: String) : AppNavKey

@Serializable
data object ReaderKey : AppNavKey

@Serializable
data object TafseerKey : AppNavKey

val TOP_LEVEL_NAV_KEYS: List<TopLevelNavKey> = listOf(
    HomeKey,
    QuranKey,
    HadithRootKey,
    StoriesKey,
    BookmarksKey,
    SettingsKey,
)

fun AppNavKey.isTopLevel(): Boolean = this is TopLevelNavKey

fun AppNavKey.topLevelKey(): TopLevelNavKey = when (this) {
    is TopLevelNavKey -> this
    is HadithBooksKey, is HadithListKey -> HadithRootKey
    ReaderKey, TafseerKey -> error("Overlay keys do not map to a top-level tab")
}
