package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = SacredTeal,
    onPrimary = Color.White,
    primaryContainer = SacredTealContainer,
    onPrimaryContainer = OnSacredTealContainer,
    secondary = SacredGold,
    onSecondary = Color.White,
    secondaryContainer = SacredGoldContainer,
    onSecondaryContainer = OnSacredGoldContainer,
    tertiary = SacredTertiary,
    onTertiary = Color.White,
    tertiaryContainer = SacredTertiaryContainer,
    onTertiaryContainer = OnSacredTertiaryContainer,
    background = SacredBackground,
    onBackground = SacredOnBackground,
    surface = SacredSurface,
    onSurface = SacredOnSurface,
    surfaceVariant = SacredSurfaceVariant,
    onSurfaceVariant = SacredOnSurfaceVariant,
    outline = SacredOutline,
    outlineVariant = SacredOutlineVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color.Black,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = Color.Black,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = SacredTertiary,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = SacredOutline,
    outlineVariant = SacredOutlineVariant
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
