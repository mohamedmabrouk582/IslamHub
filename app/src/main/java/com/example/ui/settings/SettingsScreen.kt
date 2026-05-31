package com.example.ui.settings

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.*
import com.example.ui.navigation.HomeKey
import com.example.ui.navigation.LocalAppNavigator
import com.example.viewmodel.QuranViewModel
import com.example.ui.components.SectionTitle
import com.example.ui.components.ToggleItem

@Composable
fun SettingsScreen(viewModel: QuranViewModel) {
    val navigator = LocalAppNavigator.current
    val fontSizeProgress by viewModel.fontSizeProgress.collectAsState()
    val showTranslation by viewModel.showTranslation.collectAsState()
    val showTransliteration by viewModel.showTransliteration.collectAsState()
    val nightReadingMode by viewModel.nightReadingMode.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val dailyNotification by viewModel.dailyAyahNotification.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Title Header
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp)
        ) {
            IconButton(onClick = { navigator.navigateToTab(HomeKey) }) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.primary)
            }
            Text(
                "Settings",
                style = HeadlineLgMobile,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = {}) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
            }
        }

        // Display Section
        SectionTitle("Display Settings")
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Font Size Range
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FormatSize, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Arabic Font Size", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    val label = when {
                        fontSizeProgress > 80f -> "Large"
                        fontSizeProgress > 40f -> "Medium"
                        else -> "Small"
                    }
                    Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Slider(
                    value = fontSizeProgress,
                    onValueChange = { viewModel.updateFontSize(it) },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Small", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Medium", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Large", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                // Translation toggle
                ToggleItem(
                    icon = Icons.Default.Translate,
                    title = "Translation",
                    checked = showTranslation,
                    onCheckedChange = { viewModel.toggleTranslation(it) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                // Transliteration toggle
                ToggleItem(
                    icon = Icons.Default.Subtitles,
                    title = "Transliteration",
                    checked = showTransliteration,
                    onCheckedChange = { viewModel.toggleTransliteration(it) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                // Night reading mode
                ToggleItem(
                    icon = Icons.Default.AutoStories,
                    title = "Night Reading Mode",
                    checked = nightReadingMode,
                    onCheckedChange = { viewModel.toggleNightReadingMode(it) }
                )
            }
        }

        // Audio Settings
        SectionTitle("Audio")
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { Toast.makeText(context, "Use reader to toggle reciters", Toast.LENGTH_SHORT).show() }
                ) {
                    Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Default Reciter", fontWeight = FontWeight.Bold)
                        val reciter by viewModel.selectedReciter.collectAsState()
                        Text(reciter.nameEnglish, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.outlineVariant)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                ToggleItem(
                    icon = Icons.Default.FastForward,
                    title = "Auto-play Next Surah",
                    checked = true,
                    onCheckedChange = {}
                )
            }
        }

        // Tafseer Settings
        SectionTitle("Tafseer")
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { Toast.makeText(context, "Tap any verse in reader to open Tafseer options", Toast.LENGTH_SHORT).show() }
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Book, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Default Source", fontWeight = FontWeight.Bold)
                        val tafseerSource by viewModel.selectedTafseerSource.collectAsState()
                        Text("Tafsir $tafseerSource", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }

        // Language settings
        SectionTitle("Language")
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { Toast.makeText(context, "English (Sahih International) configured", Toast.LENGTH_SHORT).show() }
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Translation Language", fontWeight = FontWeight.Bold)
                        val lang by viewModel.translationLanguage.collectAsState()
                        Text(lang, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }

        // App Settings
        SectionTitle("App Settings")
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ToggleItem(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Mode",
                    checked = isDarkTheme,
                    onCheckedChange = { viewModel.toggleDarkTheme(it) },
                    testTag = "settings_dark_mode"
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                ToggleItem(
                    icon = Icons.Default.NotificationsActive,
                    title = "Daily Ayah Notification",
                    checked = dailyNotification,
                    onCheckedChange = { viewModel.toggleDailyNotification(it) }
                )
            }
        }

        // Version & footers
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text("Privacy Policy", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.clickable {})
            Text("  •  ", fontSize = 11.sp, color = MaterialTheme.colorScheme.outlineVariant)
            Text("Terms of Service", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.clickable {})
            Text("  •  ", fontSize = 11.sp, color = MaterialTheme.colorScheme.outlineVariant)
            Text("Version 1.4.0", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(44.dp))
    }
}
