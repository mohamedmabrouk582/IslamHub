package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.snapshots.SnapshotStateList

class AppNavigator(
    private val topLevelBackStack: TopLevelBackStack<AppNavKey>,
) {
    val backStack: SnapshotStateList<AppNavKey> = topLevelBackStack.backStack
    val topLevelKey: AppNavKey get() = topLevelBackStack.topLevelKey

    fun currentKey(): AppNavKey? = topLevelBackStack.currentDestination()

    fun navigateToTab(key: TopLevelNavKey) {
        if (key == HadithRootKey) {
            topLevelBackStack.resetTopLevel(HadithRootKey)
        }
        topLevelBackStack.addTopLevel(key)
    }

    fun push(key: AppNavKey) {
        topLevelBackStack.add(key)
    }

    fun openReader() {
        topLevelBackStack.replaceOrAdd(ReaderKey) { it is ReaderKey }
    }

    fun openHadithList(slug: String, bookNumber: String) {
        topLevelBackStack.replaceOrAdd(HadithListKey(slug, bookNumber)) { it is HadithListKey }
    }

    fun pop() {
        topLevelBackStack.removeLast()
    }
}

val LocalAppNavigator = staticCompositionLocalOf<AppNavigator> {
    error("AppNavigator not provided")
}

@Composable
fun rememberAppNavigator(navigationViewModel: NavigationViewModel): AppNavigator {
    return navigationViewModel.navigator
}
