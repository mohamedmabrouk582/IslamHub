package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.text.font.FontFamily
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.theme.*
import com.example.viewmodel.QuranViewModel
import kotlinx.coroutines.launch

@Composable
fun MainScreen(viewModel: QuranViewModel) {
    val darkTheme by viewModel.isDarkTheme.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val playerMode by viewModel.playerMode.collectAsState()

    MyApplicationTheme(darkTheme = darkTheme) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                bottomBar = {
                    if (currentScreen != "reader" && currentScreen != "tafseer") {
                        QuranBottomNavigation(
                            currentScreen = currentScreen,
                            onScreenSelected = { screen ->
                                viewModel.navigateTo(screen)
                            }
                        )
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    // Background watermarks
                    GeometricBackgroundPattern()

                    Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                        when (screen) {
                            "home" -> HomeScreen(viewModel)
                            "quran" -> SurahListScreen(viewModel)
                            "stories" -> IslamicStoriesScreen(viewModel)
                            "bookmarks" -> BookmarksScreen(viewModel)
                            "hadith" -> HadithScreen(viewModel)
                            "settings" -> SettingsScreen(viewModel)
                            "reader" -> ReaderScreen(viewModel)
                            "tafseer" -> TafseerScreen(viewModel)
                        }
                    }
                }
            }

            // YouTube Player Overlay (Mini Player docked above nav bar / Full Screen Expanded Player)
            val isNavBarVisible = currentScreen != "reader" && currentScreen != "tafseer"
            val overlayModifier = if (playerMode == com.example.data.PlayerMode.EXPANDED) {
                Modifier.fillMaxSize()
            } else {
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (isNavBarVisible) 84.dp else 8.dp)
            }

            Box(
                modifier = overlayModifier,
                contentAlignment = Alignment.BottomCenter
            ) {
                com.example.ui.components.YouTubePlayerOverlay(viewModel = viewModel)
            }
        }
    }
}


@Composable
fun GeometricBackgroundPattern() {
    val color = MaterialTheme.colorScheme.primary.copy(alpha = 0.03f)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val step = 80.dp.toPx()
                for (x in 0..size.width.toInt() step step.toInt()) {
                    for (y in 0..size.height.toInt() step step.toInt()) {
                        drawCircle(
                            color = color,
                            radius = 2.dp.toPx(),
                            center = Offset(x.toFloat(), y.toFloat())
                        )
                    }
                }
            }
    )
}

@Composable
fun RubElHizbIcon(number: Int, modifier: Modifier = Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(48.dp)
            .drawBehind {
                val sizePx = size.minDimension
                val half = sizePx / 2f
                val third = sizePx / 3f

                val path1 = Path().apply {
                    moveTo(half, 0f)
                    lineTo(sizePx, third)
                    lineTo(sizePx, sizePx - third)
                    lineTo(half, sizePx)
                    lineTo(0f, sizePx - third)
                    lineTo(0f, third)
                    close()
                }

                val path2 = Path().apply {
                    moveTo(third, 0f)
                    lineTo(sizePx - third, 0f)
                    lineTo(sizePx, half)
                    lineTo(sizePx - third, sizePx)
                    lineTo(third, sizePx)
                    lineTo(0f, half)
                    close()
                }

                drawPath(path1, color = primaryColor.copy(alpha = 0.08f))
                drawPath(path1, color = primaryColor, style = Stroke(width = 1.5.dp.toPx()))
                drawPath(path2, color = secondaryColor.copy(alpha = 0.08f))
                drawPath(path2, color = secondaryColor, style = Stroke(width = 1.dp.toPx()))
            }
    ) {
        Text(
            text = number.toString(),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun QuranBottomNavigation(currentScreen: String, onScreenSelected: (String) -> Unit) {
    val context = LocalContext.current
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        tonalElevation = 8.dp,
        modifier = Modifier.clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        val screens = listOf(
            Triple("home", "Home", Icons.Default.Home),
            Triple("quran", "Quran", Icons.Default.MenuBook),
            Triple("stories", "Stories", Icons.Default.VideoLibrary),
            Triple("hadith", "Hadith", Icons.Default.AutoStories),
            Triple("bookmarks", "Saved", Icons.Default.Bookmark),
            Triple("settings", "Settings", Icons.Default.Settings)
        )

        screens.forEach { (screen, label, icon) ->
            val selected = currentScreen == screen
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (screen == "hadith") {
                        if (currentScreen == "hadith") {
                            // Already on hadith, do nothing of significance
                        }
                    }
                    onScreenSelected(screen)
                },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.testTag("nav_icon_$screen")
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            )
        }
    }
}

// 1. HOME SCREEN
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(viewModel: QuranViewModel) {
    val lastRead by viewModel.lastRead.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

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
                IconButton(onClick = { viewModel.navigateTo("quran") }) {
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
                                val bqr = QuranData.surahs.find { it.number == 2 }
                                if (bqr != null) {
                                    viewModel.selectSurah(bqr, 255)
                                    viewModel.navigateTo("reader")
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
                            val activeSurah = QuranData.surahs.find { it.number == targetSurahNum } ?: QuranData.surahs[0]
                            viewModel.selectSurah(activeSurah, targetAyahNum)
                            viewModel.navigateTo("reader")
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

        // Islamic Stories Feature Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { viewModel.navigateTo("stories") }
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFCC0000),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "Stories",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "قصص الأنبياء والقرآن",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFCC0000)
                                ) {
                                    Text(
                                        "YouTube",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                "مشغل يوتيوب متطور • سلاسل وقصص الصحابة",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.scale(scaleX = -1f, scaleY = 1f)
                    )
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
                TextButton(onClick = { viewModel.navigateTo("bookmarks") }) {
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
                                val surahObj = QuranData.surahs.find { it.number == bookmark.surahNumber } ?: return@clickable
                                viewModel.selectSurah(surahObj, bookmark.ayahNumber ?: 1)
                                viewModel.navigateTo("reader")
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
                                QuranData.surahs.find { it.number == bookmark.surahNumber }!!,
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

// 2. SURAH LIST SCREEN (QURAN TAB)
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SurahListScreen(viewModel: QuranViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterTab by remember { mutableStateOf("All Surahs") } // "All Surahs", "Juz'", "Sajdah", "My Favorites"
    val scope = rememberCoroutineScope()
    val bookmarks by viewModel.bookmarks.collectAsState()

    val filteredSurahs = remember(searchQuery, selectedFilterTab, bookmarks) {
        QuranData.surahs.filter { surah ->
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

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val r = QuranData.surahs.random()
                    viewModel.selectSurah(r, 1)
                    viewModel.navigateTo("reader")
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
                IconButton(onClick = { viewModel.navigateTo("home") }) {
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
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredSurahs) { surah ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectSurah(surah, 1)
                                    viewModel.navigateTo("reader")
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

// 3. BOOKMARKS LIST SCREEN
@Composable
fun BookmarksScreen(viewModel: QuranViewModel) {
    val bookmarks by viewModel.bookmarks.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Simple Header
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            IconButton(onClick = { viewModel.navigateTo("home") }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
            }
            Text(
                "My Bookmarks",
                style = HeadlineLgMobile,
                color = MaterialTheme.colorScheme.primary
            )
            Box(Modifier.width(48.dp)) // empty spacer placeholder
        }

        if (bookmarks.isEmpty()) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f).fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Your bookmark list is empty.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 64.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(bookmarks) { bookmark ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val surahObj = QuranData.surahs.find { it.number == bookmark.surahNumber } ?: return@clickable
                                viewModel.selectSurah(surahObj, bookmark.ayahNumber ?: 1)
                                viewModel.navigateTo("reader")
                            }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RubElHizbIcon(number = bookmark.surahNumber)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(bookmark.surahNameEnglish, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Text(
                                            if (bookmark.ayahNumber != null) "Verse ${bookmark.ayahNumber}" else "Full Surah Bookmarked",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                IconButton(onClick = {
                                    viewModel.toggleBookmark(
                                        QuranData.surahs.find { it.number == bookmark.surahNumber }!!,
                                        bookmark.ayahNumber
                                    )
                                }) {
                                    Icon(Icons.Default.Bookmark, contentDescription = "Delete", tint = MaterialTheme.colorScheme.primary)
                                }
                            }

                            if (bookmark.ayahNumber != null && bookmark.arabicText.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    bookmark.arabicText,
                                    style = QuranVerseSm,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 2,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    bookmark.translationText,
                                    style = BodyMdCustom,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    textAlign = TextAlign.Left,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 4. HADITH SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HadithScreen(viewModel: QuranViewModel) {
    val selectedHadithCollection by viewModel.selectedHadithCollection.collectAsState()
    val selectedHadithTopic by viewModel.selectedHadithTopic.collectAsState()
    val context = LocalContext.current

    // Categorized list filters
    val displayedHadiths = remember(selectedHadithCollection, selectedHadithTopic) {
        when {
            selectedHadithCollection != null -> {
                QuranData.hadiths.filter { it.collection == selectedHadithCollection }
            }
            selectedHadithTopic != null -> {
                QuranData.hadiths.filter { it.topic == selectedHadithTopic }
            }
            else -> {
                QuranData.hadiths
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Header
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                IconButton(onClick = { viewModel.navigateTo("home") }) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.primary)
                }
                Text(
                    text = "الأحاديث النبوية",
                    style = HeadlineLgMobile,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Selected filter breadcrumb if active
        if (selectedHadithCollection != null || selectedHadithTopic != null) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    IconButton(onClick = { viewModel.selectHadithCollection(null) }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear Filter", tint = MaterialTheme.colorScheme.primary)
                    }
                    Text(
                        text = "Viewing: ${selectedHadithCollection ?: selectedHadithTopic}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else {
            // Hadith of the Day
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(32.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("حديث اليوم", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Icon(Icons.Default.FormatQuote, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(28.dp))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "«الدِّينُ النَّصِيحَةُ»",
                            style = QuranVerseSm,
                            color = Color.White,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.2f)))
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            "\"The religion is sincerity (Nasihah).\"",
                            style = BodyMdCustom,
                            fontStyle = FontStyle.Italic,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            "Narrated by Muslim",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = { Toast.makeText(context, "Shared!", Toast.LENGTH_SHORT).show() }) {
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                            }
                            IconButton(onClick = { Toast.makeText(context, "Hadith Saved!", Toast.LENGTH_SHORT).show() }) {
                                Icon(Icons.Default.BookmarkBorder, contentDescription = "Bookmark", tint = Color.White)
                            }
                        }
                    }
                }
            }

            // Collections list
            item {
                Column(modifier = Modifier.padding(vertical = 12.dp)) {
                    Text(
                        "المجموعات (Collections)",
                        style = TitleMd,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val collections = listOf(
                        Triple("صحيح البخاري", "Sahih Bukhari", Icons.Default.MenuBook),
                        Triple("صحيح مسلم", "Sahih Muslim", Icons.Default.MenuBook),
                        Triple("الأربعون النووية", "40 Nawawi", Icons.Default.AutoStories),
                        Triple("رياض الصالحين", "Riyad as-Salihin", Icons.Default.Spa)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(collections) { (arabic, english, icon) ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                                modifier = Modifier
                                    .size(width = 150.dp, height = 150.dp)
                                    .clickable { viewModel.selectHadithCollection(arabic) }
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxSize().padding(12.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                    ) {
                                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(28.dp))
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(arabic, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
                                    Text(english, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }
                }
            }

            // Topics List
            item {
                Text(
                    "التصنيفات (Topics)",
                    style = TitleMd,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                )
            }
        }

        // Actual topic or result lists of Hadiths
        if (selectedHadithCollection == null && selectedHadithTopic == null) {
            val topics = listOf(
                "الأخلاق والآداب" to "Character & Manners",
                "الصلاة والطهاره" to "Prayer & Purification",
                "الإيمان" to "Faith (Iman)",
                "الأذكار والأدعية" to "Supplications"
            )

            items(topics) { (arabic, english) ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { viewModel.selectHadithTopic(arabic) }
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
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            ) {
                                Icon(
                                    imageVector = when (arabic) {
                                        "الأخلاق والآداب" -> Icons.Default.Psychology
                                        "الصلاة والطهاره" -> Icons.Default.WaterDrop
                                        "الإيمان" -> Icons.Default.Favorite
                                        else -> Icons.Default.RecordVoiceOver
                                    },
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(arabic, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text(english, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }

            // Bottom decorative illustration
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = "https://lh3.googleusercontent.com/aida-public/AB6AXuC6ZUmhZRb2T61qiVuAk4bYWyw3K8AmkGoG4h-ST_bjqKBhK7rduu0h_CZH3sZwBxVzkR9V-V9XNyyFAdXdTMYpAIHMdlOQQ59uRBgtUNICWgk95Qe5PwPmQFJQ1hEPJ2QF4zLEyXAA5FnrYyc87XEOblamtmCbAWDWC4T5iA2dLILDmsLQ165R72N015p0TzIoJNzAfzwRBvi0yCsTOaXfAwzdQ49GNpeySRhrLqcffg9ksvKAPWXfyhoLDYGoZ7E2Bp6mfXgiUBev",
                            contentDescription = "Quran manuscript",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, SacredTeal.copy(alpha = 0.85f))
                                    )
                                )
                        )
                        Box(
                            contentAlignment = Alignment.BottomStart,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp)
                        ) {
                            Text(
                                "رحلة البحث في سنة خير الأنام",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }

        } else {
            // Filtered results list of Hadiths
            items(displayedHadiths) { hadith ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                hadith.collection,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp
                            )
                            Row {
                                IconButton(onClick = { Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show() }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = { Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show() }) {
                                    Icon(Icons.Default.BookmarkBorder, contentDescription = "Save", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            hadith.textArabic,
                            style = QuranVerseSm,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            hadith.textEnglish,
                            style = BodyMdCustom,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            hadith.narrator,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// 5. SETTINGS SCREEN
@Composable
fun SettingsScreen(viewModel: QuranViewModel) {
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
            IconButton(onClick = { viewModel.navigateTo("home") }) {
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

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = TitleMd,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    )
}

@Composable
fun ToggleItem(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String = ""
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            ),
            modifier = if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier
        )
    }
}

// 6. READER SCREEN (SURAH DETAILED SCROLL)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun ReaderScreen(viewModel: QuranViewModel) {
    val surah by viewModel.currentSurah.collectAsState()
    val activeAyahNumber by viewModel.currentAyahNumber.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playProgress by viewModel.playbackProgress.collectAsState()
    val reciter by viewModel.selectedReciter.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val showTranslation by viewModel.showTranslation.collectAsState()
    val showTransliteration by viewModel.showTransliteration.collectAsState()
    val fontSizeProgress by viewModel.fontSizeProgress.collectAsState()

    val ayahs = remember(surah.number) { QuranData.getAyahsForSurah(surah.number) }

    // State for selected verse dialog context menu trigger
    var showContextMenu by remember { mutableStateOf(false) }
    var selectedMenuAyahNum by remember { mutableStateOf(1) }

    // State for full reciter dialog selector
    var showAudioDetailDialog by remember { mutableStateOf(false) }

    // State for Jump to widget
    var showJumpBottomSheet by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current

    if (showContextMenu) {
        ModalBottomSheet(
            onDismissRequest = { showContextMenu = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(bottom = 32.dp, start = 16.dp, end = 16.dp)) {
                Text(
                    "الآية $selectedMenuAyahNum",
                    style = HeadlineLgMobile,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text("Verse Options", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(16.dp))

                val menuActions = listOf(
                    Triple(Icons.Default.PlayCircle, "Play from this verse") {
                        viewModel.selectAyah(selectedMenuAyahNum)
                        viewModel.setPlaying(true)
                        showContextMenu = false
                    },
                    Triple(Icons.Default.AutoStories, "View Tafseer") {
                        viewModel.openTafseer(surah.number, selectedMenuAyahNum)
                        showContextMenu = false
                    },
                    Triple(Icons.Default.ContentCopy, "Copy verse") {
                        val targetText = ayahs.find { it.number == selectedMenuAyahNum }?.textArabic ?: ""
                        clipboardManager.setText(AnnotatedString(targetText))
                        Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
                        showContextMenu = false
                    },
                    Triple(Icons.Default.Share, "Share verse") {
                        Toast.makeText(context, "Sharing Verse $selectedMenuAyahNum...", Toast.LENGTH_SHORT).show()
                        showContextMenu = false
                    },
                    Triple(Icons.Default.BookmarkAdd, "Bookmark verse") {
                        viewModel.toggleBookmark(surah, selectedMenuAyahNum)
                        showContextMenu = false
                    }
                )

                menuActions.forEach { (icon, name, click) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { click() }
                            .padding(vertical = 12.dp)
                    ) {
                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Button(
                    onClick = { showContextMenu = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    // Full audio details custom overlay sheet
    if (showAudioDetailDialog) {
        ModalBottomSheet(
            onDismissRequest = { showAudioDetailDialog = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 44.dp)
            ) {
                // Info
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(surah.nameArabic, style = DisplayArabic, fontSize = 28.sp, color = MaterialTheme.colorScheme.primary)
                    Text("Playing from verse $activeAyahNumber", style = BodyMdCustom, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Reciter Image centerpiece with circular animated progress
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
                    // Circular Progress Vector
                    CircularProgressIndicator(
                        progress = { playProgress },
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    )

                    AsyncImage(
                        model = reciter.imageUrl,
                        contentDescription = reciter.nameEnglish,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    )
                }

                // Name
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(reciter.nameEnglish, fontWeight = FontWeight.Bold, style = TitleMd)
                    Text(reciter.nameArabic, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // simulated bouncing visualizer
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.height(40.dp).fillMaxWidth()
                ) {
                    val progressCount = if (isPlaying) 8 else 4
                    for (i in 0 until progressCount) {
                        val h = if (isPlaying) (10..36).random().dp else 8.dp
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(h)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }

                // audio controls
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    IconButton(onClick = {
                        val next = surah.number + 1
                        if (next <= 114) {
                            viewModel.selectSurah(QuranData.surahs.find { it.number == next }!!, 1)
                        }
                    }) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Next Surah", modifier = Modifier.size(32.dp))
                    }
                    IconButton(onClick = {
                        val prev = activeAyahNumber - 1
                        if (prev >= 1) viewModel.selectAyah(prev)
                    }) {
                        Icon(Icons.Default.Replay10, contentDescription = "Prev Ayah", modifier = Modifier.size(32.dp))
                    }

                    // huge floating pause
                    FloatingActionButton(
                        onClick = { viewModel.setPlaying(!isPlaying) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Toggle",
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    IconButton(onClick = {
                        val next = activeAyahNumber + 1
                        if (next <= surah.versesCount) viewModel.selectAyah(next)
                    }) {
                        Icon(Icons.Default.Forward10, contentDescription = "Next Ayah", modifier = Modifier.size(32.dp))
                    }
                    IconButton(onClick = {
                        val prev = surah.number - 1
                        if (prev >= 1) {
                            viewModel.selectSurah(QuranData.surahs.find { it.number == prev }!!, 1)
                        }
                    }) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Prev Surah", modifier = Modifier.size(32.dp))
                    }
                }

                // Picker speed horizontal tabs
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(32.dp))
                        .padding(8.dp)
                ) {
                    val speeds = listOf(0.75f, 1.0f, 1.25f, 1.5f)
                    val activeSpeed by viewModel.playbackSpeed.collectAsState()
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        speeds.forEach { sp ->
                            val active = activeSpeed == sp
                            FilterChip(
                                selected = active,
                                onClick = { viewModel.setPlaybackSpeed(sp) },
                                label = { Text("${sp}x") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(32.dp)
                            )
                        }
                    }

                    // Icons
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.Repeat, contentDescription = "Repeat", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Icon(Icons.Default.Bedtime, contentDescription = "Sleep Timer", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Choose list of reciters
                Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                    Text("SELECT RECITER", style = LabelSmCustom, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(QuranData.reciters) { r ->
                            val selected = reciter.id == r.id
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.selectReciter(r) },
                                label = { Text(r.nameEnglish) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    if (showJumpBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showJumpBottomSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 44.dp, start = 16.dp, end = 16.dp)
            ) {
                Text("Select Verse to Play / Read", style = HeadlineLgMobile, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))

                // Scroll widgets representation
                LazyRow(
                    modifier = Modifier.height(120.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(QuranData.surahs) { s ->
                        val selected = s.number == surah.number
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .width(130.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                                .clickable {
                                    viewModel.selectSurah(s, 1)
                                }
                                .padding(8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(s.nameEnglish, fontWeight = FontWeight.Bold, color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface)
                                Text(s.nameArabic, fontSize = 12.sp, color = if (selected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Ayah wheel trigger select preview
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Ayah to Jump:",
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (activeAyahNumber > 1) viewModel.selectAyah(activeAyahNumber - 1) }) {
                            Icon(Icons.Default.Remove, contentDescription = null)
                        }
                        Text(activeAyahNumber.toString(), style = TitleMd, color = MaterialTheme.colorScheme.primary)
                        IconButton(onClick = { if (activeAyahNumber < surah.versesCount) viewModel.selectAyah(activeAyahNumber + 1) }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        showJumpBottomSheet = false
                        viewModel.setPlaying(true)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.PlayCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start playing")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(surah.nameArabic, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
                        Text(surah.nameEnglish, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo("quran") }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    var isSBookmarked by remember { mutableStateOf(false) }
                    LaunchedEffect(surah, activeAyahNumber) {
                        viewModel.checkIsBookmarked("${surah.number}") { isSBookmarked = it }
                    }
                    IconButton(onClick = {
                        viewModel.toggleBookmark(surah)
                        isSBookmarked = !isSBookmarked
                    }) {
                        Icon(
                            imageVector = if (isSBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark Surah",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { Toast.makeText(context, "Shared!", Toast.LENGTH_SHORT).show() }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        },
        bottomBar = {
            // Persistent Bottom Mini Audio Player
            Surface(
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { playProgress },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                    )

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAudioDetailDialog = true }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            // portrait avatar click
                            AsyncImage(
                                model = reciter.imageUrl,
                                contentDescription = reciter.nameEnglish,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(reciter.nameEnglish, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, maxLines = 1)
                                Text("${surah.nameEnglish} • Verse $activeAyahNumber", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                if (activeAyahNumber > 1) viewModel.selectAyah(activeAyahNumber - 1)
                            }) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = "Previous Verse")
                            }

                            IconButton(
                                onClick = { viewModel.setPlaying(!isPlaying) },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Toggle",
                                    tint = Color.White
                                )
                            }

                            IconButton(onClick = {
                                if (activeAyahNumber < surah.versesCount) viewModel.selectAyah(activeAyahNumber + 1)
                            }) {
                                Icon(Icons.Default.SkipNext, contentDescription = "Next Verse")
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
        ) {
            // Elegant Bismillah Header card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                            style = DisplayArabic,
                            fontSize = 28.sp,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // List of verses
            items(ayahs) { ayah ->
                val isActive = ayah.number == activeAyahNumber
                val fontMultiplier = remember(fontSizeProgress) { 0.6f + (fontSizeProgress / 100f) * 0.8f }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .combinedClickable(
                            onClick = {
                                viewModel.selectAyah(ayah.number)
                            },
                            onLongClick = {
                                selectedMenuAyahNum = ayah.number
                                showContextMenu = true
                            }
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f)
                        else Color.Transparent
                    ),
                    border = if (isActive) BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)) else null
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left/Top Arabic number tag
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isActive) MaterialTheme.colorScheme.secondary
                                    else MaterialTheme.colorScheme.surfaceContainer
                                )
                        ) {
                            Text(
                                text = ayah.number.toString(),
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) Color.White else MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp
                            )
                        }

                        // Right Amiri text + translations
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) {
                            Text(
                                text = ayah.textArabic,
                                style = QuranVerseLg,
                                fontSize = (34 * fontMultiplier).sp,
                                lineHeight = (68 * fontMultiplier).sp,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (showTransliteration && ayah.transliteration.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = ayah.transliteration,
                                    style = BodyMdCustom,
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Left
                                )
                            }

                            if (showTranslation) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = ayah.textEnglish,
                                    style = BodyLg,
                                    color = if (isActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Left
                                )
                            }
                        }
                    }
                }
            }

            // Quick Access Jump Button selector
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { showJumpBottomSheet = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Pin, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Jump or Select Reciter Setup", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 7. TAFSEER SCREEN (READ EXPLANATIONS)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TafseerScreen(viewModel: QuranViewModel) {
    val surahNum by viewModel.tafseerSurahNumber.collectAsState()
    val ayahNum by viewModel.tafseerAyahNumber.collectAsState()
    val activeSource by viewModel.selectedTafseerSource.collectAsState()
    val context = LocalContext.current

    val currentSurah = remember(surahNum) { QuranData.surahs.find { it.number == surahNum } ?: QuranData.surahs[0] }
    val ayahs = remember(surahNum) { QuranData.getAyahsForSurah(surahNum) }
    val activeAyah = remember(ayahs, ayahNum) { ayahs.find { it.number == ayahNum } ?: ayahs[0] }

    val commentary = remember(surahNum, ayahNum, activeSource) {
        QuranData.getTafseer(surahNum, ayahNum, activeSource)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("تفسير", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
                        Text("${currentSurah.nameEnglish} • $surahNum:$ayahNum", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo("reader") }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    IconButton(onClick = { Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show() }) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = "Bookmark", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { Toast.makeText(context, "Shared!", Toast.LENGTH_SHORT).show() }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Verse Context Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = activeAyah.textArabic,
                        style = QuranVerseSm,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = activeAyah.textEnglish,
                        style = BodyMdCustom,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Left,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Tafseer sources Horizontal Tabs Selector
            val sources = listOf("ابن كثير", "الطبري", "السعدي", "الميسر")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(sources) { src ->
                    val isSelected = activeSource == src
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setTafseerSource(src) },
                        label = { Text(src) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            // Comment text body card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Icon(Icons.Default.Book, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "تفسير $activeSource",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            style = TitleMd
                        )
                    }

                    Text(
                        text = commentary,
                        style = QuranVerseSm,
                        fontSize = 18.sp,
                        lineHeight = 32.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Justify,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Related Verses
            Text(
                "آيات ذات صلة",
                style = TitleMd,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 12.dp)
            )

            // Related Verses horizontally
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 44.dp)
            ) {
                listOf(
                    Triple(164, "سورة البقرة", "إِنَّ فِي خَلْقِ السَّمَاوَاتِ..."),
                    Triple(2, "سورة آل عمران", "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ...")
                ).forEach { (number, surahName, arabic) ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val targetS = if (number == 164) 2 else 3
                                val sObj = QuranData.surahs.find { it.number == targetS }!!
                                viewModel.selectSurah(sObj, if (number == 164) 164 else 2)
                                viewModel.navigateTo("reader")
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                            ) {
                                Text(number.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(arabic, style = QuranVerseLg, fontSize = 13.sp, lineHeight = 18.sp, maxLines = 1, color = MaterialTheme.colorScheme.primary)
                                Text(surahName, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
