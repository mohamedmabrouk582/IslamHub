package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldState
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.AppNavigator
import com.example.ui.navigation.BookmarksKey
import com.example.ui.navigation.HadithRootKey
import com.example.ui.navigation.HomeKey
import com.example.ui.navigation.QuranKey
import com.example.ui.navigation.SettingsKey
import com.example.ui.navigation.TopLevelNavKey

private data class TopLevelDestination(
    val key: TopLevelNavKey,
    val label: String,
    val icon: ImageVector,
)

private val topLevelDestinations = listOf(
    TopLevelDestination(HomeKey, "Home", Icons.Default.Home),
    TopLevelDestination(QuranKey, "Quran", Icons.Default.MenuBook),
    TopLevelDestination(HadithRootKey, "Hadith", Icons.Default.AutoStories),
    TopLevelDestination(BookmarksKey, "Bookmarks", Icons.Default.Bookmark),
    TopLevelDestination(SettingsKey, "Settings", Icons.Default.Settings),
)

@Composable
fun AdaptiveAppScaffold(
    navigator: AppNavigator,
    modifier: Modifier = Modifier,
    scaffoldState: NavigationSuiteScaffoldState = rememberNavigationSuiteScaffoldState(),
    content: @Composable () -> Unit,
) {
    val navigationBarItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
    )
    val navigationSuiteItemColors = NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = navigationBarItemColors,
    )

    NavigationSuiteScaffold(
        modifier = modifier,
        state = scaffoldState,
        navigationSuiteItems = {
            topLevelDestinations.forEach { destination ->
                val selected = navigator.topLevelKey == destination.key
                item(
                    selected = selected,
                    onClick = { navigator.navigateToTab(destination.key) },
                    icon = {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label,
                        )
                    },
                    label = {
                        Text(
                            text = destination.label,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                    colors = navigationSuiteItemColors,
                )
            }
        },
        content = { content() },
    )
}
