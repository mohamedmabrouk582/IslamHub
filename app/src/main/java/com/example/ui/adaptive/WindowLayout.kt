package com.example.ui.adaptive

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration

data class WindowLayout(
    val supportsListDetail: Boolean,
    val isCompactWidth: Boolean,
    val isCompactHeight: Boolean,
    val contentMaxWidthDp: Int,
    val horizontalContentPaddingDp: Int,
)

val LocalWindowLayout = compositionLocalOf<WindowLayout> {
    error("WindowLayout not provided")
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun rememberWindowLayout(): WindowLayout {
    val configuration = LocalConfiguration.current
    val windowAdaptiveInfo = currentWindowAdaptiveInfoV2()
    return remember(windowAdaptiveInfo, configuration.screenWidthDp, configuration.screenHeightDp) {
        val directive = calculatePaneScaffoldDirective(windowAdaptiveInfo)
        val widthDp = configuration.screenWidthDp
        val heightDp = configuration.screenHeightDp
        WindowLayout(
            supportsListDetail = directive.maxHorizontalPartitions >= 2,
            isCompactWidth = directive.maxHorizontalPartitions < 2,
            isCompactHeight = heightDp < 480,
            contentMaxWidthDp = when {
                widthDp >= 840 -> 840
                widthDp >= 600 -> 720
                else -> Int.MAX_VALUE
            },
            horizontalContentPaddingDp = when {
                widthDp >= 840 -> 32
                widthDp >= 600 -> 24
                else -> 16
            },
        )
    }
}
