package com.example.ui.quran

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.ui.common.UiState
import com.example.ui.components.LoadingErrorContent
import com.example.ui.components.RubElHizbIcon

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SurahListScreen(viewModel: QuranViewModel) {
    val navigator = LocalAppNavigator.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterTab by remember { mutableStateOf("All Surahs") } // "All Surahs", "Juz'", "Sajdah", "My Favorites"
    val bookmarks by viewModel.bookmarks.collectAsState()
    val currentSurah by viewModel.currentSurah.collectAsState()
    val surahsState by viewModel.surahs.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.loadSurahs()
    }

    LoadingErrorContent(
        state = surahsState,
        onRetry = { viewModel.loadSurahs() },
    ) { surahs ->
        val filteredSurahs = remember(searchQuery, selectedFilterTab, bookmarks, surahs) {
            surahs.filter { surah ->
            // Apply search filter
            val matchesQuery = surah.nameEnglish.contains(searchQuery, ignoreCase = true) ||
                    surah.nameArabic.contains(searchQuery) ||
                    surah.number.toString() == searchQuery

            // Apply tab filter
            val matchesTab = when (selectedFilterTab) {
                "Juz'" -> surah.juzNumber > 0 // standard filter representation
                "Sajdah" -> surah.hasSajdah
                "My Favorites" -> bookmarks.any { it.surahNumber == surah.number && it.ayahNumber == null }
                else -> true
            }

            matchesQuery && matchesTab
        }
    }

    LaunchedEffect(currentSurah.number, filteredSurahs) {
        val selectedIndex = filteredSurahs.indexOfFirst { it.number == currentSurah.number }
        if (selectedIndex >= 0) {
            listState.animateScrollToItem(selectedIndex)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val r = surahs.random()
                    viewModel.selectSurah(r, 1)
                    navigator.openReader()
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Numbers, contentDescription = "Jump to Surah")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                IconButton(onClick = { navigator.navigateToTab(HomeKey) }) {
                    Icon(Icons.Default.Menu, contentDescription = "Home", tint = MaterialTheme.colorScheme.primary)
                }
                Text(
                    text = "القرآن الكريم",
                    style = HeadlineLgMobile,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                }
            }

            // Search textfield
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search Surah by name or number...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.outline) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Horizontal Filter tabs
            val tabs = listOf("All Surahs", "Juz'", "Sajdah", "My Favorites")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                items(tabs) { tab ->
                    val isSelected = selectedFilterTab == tab
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilterTab = tab },
                        label = { Text(tab, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(32.dp),
                        border = null
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Surahs list
            if (filteredSurahs.isEmpty()) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f).fillMaxWidth()) {
                    Text("No chapters found matching search criteria.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredSurahs, key = { it.number }) { surah ->
                        val isSelected = surah.number == currentSurah.number
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                                },
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectSurah(surah, 1)
                                    navigator.openReader()
                                }
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RubElHizbIcon(number = surah.number)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(surah.nameEnglish, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(32.dp))
                                                    .background(
                                                        if (surah.type == "MECCAN") MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                                        else MaterialTheme.colorScheme.secondary.copy(alpha = 0.13f)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    surah.type,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (surah.type == "MECCAN") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                        }
                                        Text("${surah.translation} • ${surah.versesCount} Verses", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                // Arabic Name Calligraphy Representation
                                Text(
                                    surah.nameArabic,
                                    style = DisplayArabic,
                                    fontSize = 24.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    }
}
