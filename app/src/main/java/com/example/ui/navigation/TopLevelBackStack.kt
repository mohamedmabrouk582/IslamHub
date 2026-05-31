package com.example.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList

class TopLevelBackStack<T : Any>(startKey: T) {
    private val topLevelStacks: LinkedHashMap<T, SnapshotStateList<T>> = linkedMapOf(
        startKey to mutableStateListOf(startKey),
    )

    var topLevelKey by mutableStateOf(startKey)
        private set

    val backStack = mutableStateListOf(startKey)

    private var onChanged: (() -> Unit)? = null

    fun setOnChanged(listener: () -> Unit) {
        onChanged = listener
    }

    fun snapshot(): NavigationSnapshot {
        @Suppress("UNCHECKED_CAST")
        val self = this as TopLevelBackStack<AppNavKey>
        return NavigationSnapshot(
            backStack = self.backStack.toList(),
            topLevelKey = self.topLevelKey as TopLevelNavKey,
        )
    }

    fun restore(snapshot: NavigationSnapshot) {
        @Suppress("UNCHECKED_CAST")
        val self = this as TopLevelBackStack<AppNavKey>
        val stacks = buildStacksFromFlat(snapshot.backStack)
        self.topLevelStacks.clear()
        stacks.forEach { (key, entries) ->
            self.topLevelStacks[key] = mutableStateListOf(*entries.toTypedArray())
        }
        if (self.topLevelStacks.isEmpty()) {
            self.topLevelStacks[HomeKey] = mutableStateListOf(HomeKey)
        }
        self.topLevelKey = snapshot.topLevelKey
        self.updateBackStack()
    }

    fun currentDestination(): T? = topLevelStacks[topLevelKey]?.lastOrNull()

    private fun updateBackStack() {
        backStack.apply {
            clear()
            addAll(topLevelStacks.flatMap { it.value })
        }
        onChanged?.invoke()
    }

    fun resetTopLevel(key: T) {
        topLevelStacks[key] = mutableStateListOf(key)
        topLevelKey = key
        updateBackStack()
    }

    fun addTopLevel(key: T) {
        if (topLevelStacks[key] == null) {
            topLevelStacks[key] = mutableStateListOf(key)
        } else {
            topLevelStacks.apply {
                remove(key)?.let { put(key, it) }
            }
        }
        topLevelKey = key
        updateBackStack()
    }

    fun add(key: T) {
        topLevelStacks[topLevelKey]?.add(key)
        updateBackStack()
    }

    fun replaceOrAdd(key: T, shouldRemove: (T) -> Boolean) {
        val stack = topLevelStacks[topLevelKey] ?: return
        stack.removeAll(shouldRemove)
        stack.add(key)
        updateBackStack()
    }

    fun removeLast() {
        val removedKey = topLevelStacks[topLevelKey]?.removeLastOrNull()
        topLevelStacks.remove(removedKey)
        topLevelKey = topLevelStacks.keys.lastOrNull() ?: topLevelKey
        updateBackStack()
    }
}
