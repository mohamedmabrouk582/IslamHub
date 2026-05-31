package com.example.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

object QuranListDetailSceneKey
object HadithListDetailSceneKey

fun AppNavigator.isQuranListDetailReader(supportsListDetail: Boolean): Boolean {
    if (!supportsListDetail) return false
    return backStack.contains(QuranKey) && currentKey() == ReaderKey
}

fun AppNavigator.showHadithBooksBackButton(supportsListDetail: Boolean): Boolean {
    if (!supportsListDetail) return true
    return !(backStack.contains(HadithRootKey) && currentKey() is HadithBooksKey)
}

fun AppNavigator.showHadithListBackButton(supportsListDetail: Boolean): Boolean {
    if (!supportsListDetail) return true
    return !(backStack.any { it is HadithBooksKey } && currentKey() is HadithListKey)
}

@Composable
fun QuranDetailPlaceholder() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Select a surah to read",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "اختر سورة للقراءة",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun HadithDetailPlaceholder() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Select a collection or book",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "اختر مجموعة أو كتاباً",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
        )
    }
}
