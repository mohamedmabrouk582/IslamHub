package com.example.ui.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true }

    private val topLevelBackStack = TopLevelBackStack<AppNavKey>(HomeKey)

    val navigator: AppNavigator = AppNavigator(topLevelBackStack)

    init {
        restoreFromSavedState()
        topLevelBackStack.setOnChanged { persistNavigation() }
    }

    private fun restoreFromSavedState() {
        val saved = savedStateHandle.get<String>(NAV_STATE_KEY) ?: return
        runCatching {
            val snapshot = json.decodeFromString<NavigationSnapshot>(saved)
            if (snapshot.backStack.isNotEmpty()) {
                topLevelBackStack.restore(snapshot)
            }
        }
    }

    private fun persistNavigation() {
        savedStateHandle[NAV_STATE_KEY] = json.encodeToString(topLevelBackStack.snapshot())
    }

    companion object {
        private const val NAV_STATE_KEY = "nav_state"
    }
}
