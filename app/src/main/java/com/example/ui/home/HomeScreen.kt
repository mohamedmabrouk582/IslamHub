package com.example.ui.home

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
import com.example.ui.navigation.BookmarksKey
import com.example.ui.navigation.HadithBooksKey
import com.example.ui.navigation.HomeKey
import com.example.ui.navigation.LocalAppNavigator
import com.example.ui.navigation.QuranKey
import com.example.ui.navigation.ReaderKey
import com.example.viewmodel.QuranViewModel
import com.example.data.model.Surah
import com.example.ui.common.UiState
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(viewModel: QuranViewModel) {
    val navigator = LocalAppNavigator.current
    val lastRead by viewModel.lastRead.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val context = LocalContext.current
    val surahsState by viewModel.surahs.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSurahs()
    }

    fun resolveSurah(number: Int): Surah? {
        return when (val state = surahsState) {
            is UiState.Success -> state.data.find { it.number == number }
            else -> null
        }
    }

    var showQiblaDialog by remember { mutableStateOf(false) }
    var showPrayersDialog by remember { mutableStateOf(false) }

    if (showQiblaDialog) {
        AlertDialog(
            onDismissRequest = { showQiblaDialog = false },
            confirmButton = {
                TextButton(onClick = { showQiblaDialog = false }) {
                    Text("Close", color = MaterialTheme.colorScheme.primary)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Explore, contentDescription = "Qibla Finder", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Qibla Finder", fontFamily = FontFamily.Default)
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(160.dp)
                            .drawBehind {
                                drawCircle(
                                    color = SacredTeal.copy(alpha = 0.1f),
                                    radius = size.minDimension / 2
                                )
                                drawCircle(
                                    color = SacredGold,
                                    radius = size.minDimension / 2,
                                    style = Stroke(width = 2.dp.toPx())
                                )
                            }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = "Arrow",
                                tint = SacredTeal,
                                modifier = Modifier
                                    .size(64.dp)
                                    .scale(scaleX = 1f, scaleY = -1f) // pointing north-east helper
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("134° SE", fontWeight = FontWeight.Bold, color = SacredTeal)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Point your phone east. Live compass direction towards Kaaba, Mecca is calibrated successfully.",
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (showPrayersDialog) {
        AlertDialog(
            onDismissRequest = { showPrayersDialog = false },
            confirmButton = {
                TextButton(onClick = { showPrayersDialog = false }) {
                    Text("Close", color = MaterialTheme.colorScheme.primary)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = "Prayers", tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Prayer Times", fontFamily = FontFamily.Default)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    val times = listOf(
                        "Fajr" to "4:12 AM",
                        "Sunrise" to "5:45 AM",
                        "Dhuhr" to "12:15 PM",
                        "Asr" to "3:45 PM (Active)",
                        "Maghrib" to "6:45 PM",
                        "Isha" to "8:15 PM"
                    )
                    times.forEach { (name, time) ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (name == "Asr") MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                                    else Color.Transparent
                                )
                                .padding(8.dp)
                        ) {
                            Text(
                                name,
                                fontWeight = if (name == "Asr") FontWeight.Bold else FontWeight.Normal,
                                color = if (name == "Asr") SacredTeal else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                time,
                                fontWeight = if (name == "Asr") FontWeight.Bold else FontWeight.Normal,
                                color = if (name == "Asr") SacredTeal else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // App Title Header
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                IconButton(onClick = { Toast.makeText(context, "Menu clicked", Toast.LENGTH_SHORT).show() }) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.primary)
                }
                Text(
                    text = "Al-Quran",
                    style = HeadlineLgMobile,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = { navigator.navigateToTab(QuranKey) }) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Quran Centerpiece Logo & bismillah
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                        .padding(12.dp)
                ) {
                    // Custom SVG lookalike bismillah logo icon
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = SacredGold,
                            radius = size.minDimension / 2,
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                        drawCircle(
                            color = SacredTeal,
                            radius = size.minDimension / 2.7f,
                            style = Stroke(width = 3.dp.toPx())
                        )
                        // inside core
                        drawOval(
                            color = SacredGold,
                            topLeft = Offset(size.width / 2.5f, size.height / 3f),
                            size = size / 5f
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "بسم الله الرحمن الرحيم",
                    style = DisplayArabic,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 32.sp,
                    lineHeight = 44.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "In the Name of Allah, the Gracious, the Merciful",
                    style = BodyMdCustom,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Daily Ayah Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(32.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "DAILY AYAH",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Verse of the Day",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = {
                            Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ...",
                        style = QuranVerseSm,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        textAlign = TextAlign.Right
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                            ) {
                                Text("255", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Al-Baqarah", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("The Cow • 286 Verses", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Button(
                            onClick = {
                                resolveSurah(2)?.let { bqr ->
                                    viewModel.selectSurah(bqr, 255)
                                    navigator.push(ReaderKey)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Read More", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.scale(scaleX = -1f, scaleY = 1f))
                            }
                        }
                    }
                }
            }
        }

        // Bento Grid Quick Access
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(intrinsicSize = IntrinsicSize.Max)
                    .padding(vertical = 8.dp)
            ) {
                // Last Read Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            val targetSurahNum = lastRead?.surahNumber ?: 255
                            val targetAyahNum = lastRead?.ayahNumber ?: 1
                            val activeSurah = resolveSurah(targetSurahNum) ?: when (val state = surahsState) {
                                is UiState.Success -> state.data.firstOrNull()
                                else -> null
                            }
                            activeSurah?.let { viewModel.selectSurah(it, targetAyahNum) }
                            navigator.push(ReaderKey)
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Column {
                            Text("Last Read", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            val surahText = lastRead?.surahNameEnglish ?: "Surah Yasin"
                            val verseText = lastRead?.ayahNumber ?: 12
                            Text("$surahText: Verse $verseText", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Qibla Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { showQiblaDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiary)
                        ) {
                            Icon(Icons.Default.Explore, contentDescription = null, tint = Color.White)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Column {
                            Text("Qibla", fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Find Direction", fontSize = 11.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                        }
                    }
                }
            }
        }

        // Prayer Times Bento Item
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { showPrayersDialog = true }
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.onSecondaryContainer)
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = "Clock", tint = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Prayer Times", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Text("Next: Asr • 3:45 PM", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f))
                        }
                    }

                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.scale(scaleX = -1f, scaleY = 1f))
                }
            }
        }

        // Recent Bookmarks
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 22.dp, bottom = 8.dp)
            ) {
                Text(
                    "Recent Bookmarks",
                    style = TitleMd,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = { navigator.navigateToTab(BookmarksKey) }) {
                    Text("VIEW ALL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        if (bookmarks.isEmpty()) {
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        "No bookmarks yet. Tap the bookmark icon in reader to add.",
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(bookmarks.take(3)) { bookmark ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val surahObj = resolveSurah(bookmark.surahNumber) ?: return@clickable
                                viewModel.selectSurah(surahObj, bookmark.ayahNumber ?: 1)
                                navigator.push(ReaderKey)
                            }
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            ) {
                                val offsetStr = when (bookmark.surahNumber) {
                                    18 -> "١٨"
                                    67 -> "٦٧"
                                    else -> bookmark.surahNumber.toString()
                                }
                                Text(
                                    offsetStr,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                val tag = if (bookmark.ayahNumber != null) "Ayah ${bookmark.ayahNumber}" else "Full Surah"
                                Text(bookmark.surahNameEnglish, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text("${bookmark.surahName} • $tag", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        IconButton(onClick = {
                            viewModel.toggleBookmark(
                                Surah(
                                    number = bookmark.surahNumber,
                                    nameArabic = bookmark.surahName,
                                    nameEnglish = bookmark.surahNameEnglish,
                                    translation = "",
                                    type = "",
                                    versesCount = bookmark.numberOfVerses,
                                ),
                                bookmark.ayahNumber
                            )
                        }) {
                            Icon(Icons.Default.Bookmark, contentDescription = "Delete", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
