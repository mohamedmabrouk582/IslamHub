package com.example.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data class NavigationSnapshot(
    val backStack: List<AppNavKey>,
    val topLevelKey: TopLevelNavKey,
)

fun buildStacksFromFlat(flatList: List<AppNavKey>): LinkedHashMap<TopLevelNavKey, List<AppNavKey>> {
    val stacks = linkedMapOf<TopLevelNavKey, MutableList<AppNavKey>>()
    var currentTopLevel: TopLevelNavKey? = null

    for (key in flatList) {
        when (key) {
            is TopLevelNavKey -> {
                currentTopLevel = key
                stacks.getOrPut(key) { mutableListOf() }.apply {
                    clear()
                    add(key)
                }
            }
            ReaderKey, TafseerKey -> {
                val tab = currentTopLevel ?: QuranKey
                stacks.getOrPut(tab) { mutableListOf(tab) }.add(key)
            }
            is HadithBooksKey, is HadithListKey -> {
                currentTopLevel = HadithRootKey
                stacks.getOrPut(HadithRootKey) { mutableListOf(HadithRootKey) }.add(key)
            }
        }
    }
    return stacks.mapValues { it.value.toList() }.let { LinkedHashMap(it) }
}
