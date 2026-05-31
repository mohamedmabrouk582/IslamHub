package com.example.ui.reader

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.data.model.Ayah
import com.example.data.model.Surah
import com.example.ui.common.UiState
import com.example.ui.components.LoadingErrorContent
import com.example.ui.components.QuranAudioPlayerBar
import com.example.ui.components.QuranPlayerBarVariant
import com.example.ui.adaptive.LocalWindowLayout
import com.example.ui.tafseer.TafseerBottomSheet
import com.example.ui.theme.*
import com.example.ui.navigation.LocalAppNavigator
import com.example.ui.navigation.isQuranListDetailReader
import com.example.viewmodel.QuranViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun ReaderScreen(viewModel: QuranViewModel) {
    val navigator = LocalAppNavigator.current
    val windowLayout = LocalWindowLayout.current
    val showBackButton = !navigator.isQuranListDetailReader(windowLayout.supportsListDetail)
    val horizontalPadding = windowLayout.horizontalContentPaddingDp.dp
    val verseCardPadding = if (windowLayout.isCompactHeight) 10.dp else 14.dp
    val titleFontSize = if (windowLayout.isCompactHeight) 16.sp else 18.sp
    val bismillahFontSize = if (windowLayout.isCompactHeight) 24.sp else 28.sp
    val surah by viewModel.currentSurah.collectAsState()
    val activeAyahNumber by viewModel.currentAyahNumber.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playProgress by viewModel.currentVersePlaybackProgress.collectAsState()
    val reciter by viewModel.selectedReciter.collectAsState()
    val isDownloadingAudio by viewModel.isDownloadingAudio.collectAsState()
    val surahDownloadProgress by viewModel.surahDownloadProgress.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val showTranslation by viewModel.showTranslation.collectAsState()
    val showTransliteration by viewModel.showTransliteration.collectAsState()
    val readerLayoutMode by viewModel.readerLayoutMode.collectAsState()
    val fontSizeProgress by viewModel.fontSizeProgress.collectAsState()

    val currentAyahsState by viewModel.currentAyahs.collectAsState()
    val ayahCache by viewModel.ayahCache.collectAsState()
    val surahsState by viewModel.surahs.collectAsState()

    LaunchedEffect(surah.number, reciter.id) {
        viewModel.onReaderVisible()
        viewModel.prefetchAdjacentAyahs(surah.number)
    }

    fun resolveSurah(number: Int): Surah? {
        return when (val state = surahsState) {
            is UiState.Success -> state.data.find { it.number == number }
            else -> null
        }
    }

    val ayahs = when (val state = currentAyahsState) {
        is UiState.Success -> state.data
        else -> emptyList()
    }

    // State for selected verse dialog context menu trigger
    var showContextMenu by remember { mutableStateOf(false) }
    var selectedMenuAyahNum by remember { mutableStateOf(1) }

    // State for full reciter dialog selector
    var showAudioDetailDialog by remember { mutableStateOf(false) }

    // State for Jump to widget
    var showJumpBottomSheet by remember { mutableStateOf(false) }
    var showTafseerSheet by remember { mutableStateOf(false) }

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
                        showTafseerSheet = true
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

    ReaderAudioPlayerDialog(
        visible = showAudioDetailDialog,
        onDismiss = { showAudioDetailDialog = false },
        windowLayout = windowLayout,
        viewModel = viewModel,
        surah = surah,
        reciter = reciter,
        activeAyah = ayahs.find { it.number == activeAyahNumber },
        activeAyahNumber = activeAyahNumber,
        showTranslation = showTranslation,
        isPlaying = isPlaying,
        playbackProgress = playProgress,
        resolveSurah = { number -> resolveSurah(number) },
    )

    TafseerBottomSheet(
        visible = showTafseerSheet,
        onDismiss = { showTafseerSheet = false },
        viewModel = viewModel,
    )

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
                    items((surahsState as? UiState.Success)?.data.orEmpty()) { s ->
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
                        Text(
                            surah.nameArabic,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = titleFontSize,
                        )
                        if (!windowLayout.isCompactHeight) {
                            Text(surah.nameEnglish, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleReaderLayoutMode() }) {
                        Icon(
                            imageVector = if (readerLayoutMode == ReaderLayoutMode.Book) {
                                Icons.Default.ViewAgenda
                            } else {
                                Icons.Default.MenuBook
                            },
                            contentDescription = if (readerLayoutMode == ReaderLayoutMode.Book) {
                                "Switch to list view"
                            } else {
                                "Switch to book view"
                            },
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
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
            QuranAudioPlayerBar(
                surah = surah,
                reciter = reciter,
                activeAyahNumber = activeAyahNumber,
                versesCount = surah.versesCount,
                isPlaying = isPlaying,
                versePlayProgress = playProgress,
                isDownloadingAudio = isDownloadingAudio,
                surahDownloadProgress = surahDownloadProgress,
                variant = if (windowLayout.isCompactHeight) {
                    QuranPlayerBarVariant.Compact
                } else {
                    QuranPlayerBarVariant.Full
                },
                onBarClick = { showAudioDetailDialog = true },
                onPlayPause = { viewModel.setPlaying(!isPlaying) },
                onPrevious = {
                    if (activeAyahNumber > 1) viewModel.selectAyah(activeAyahNumber - 1)
                },
                onNext = {
                    if (activeAyahNumber < surah.versesCount) viewModel.selectAyah(activeAyahNumber + 1)
                },
            )
        }
    ) { innerPadding ->
        val allSurahs = when (val state = surahsState) {
            is UiState.Success -> state.data
            else -> emptyList()
        }

        if (allSurahs.isEmpty()) {
            LoadingErrorContent(
                state = currentAyahsState,
                onRetry = { viewModel.loadAyahsForCurrentSurah() },
            ) { loadedAyahs ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    ReaderAyahContent(
                        layoutMode = readerLayoutMode,
                        surahNumber = surah.number,
                        surahNameArabic = surah.nameArabic,
                        ayahs = loadedAyahs,
                        activeAyahNumber = activeAyahNumber,
                        fontSizeProgress = fontSizeProgress,
                        showTranslation = showTranslation,
                        showTransliteration = showTransliteration,
                        horizontalPadding = horizontalPadding,
                        contentMaxWidthDp = windowLayout.contentMaxWidthDp,
                        isCompactWidth = windowLayout.isCompactWidth,
                        verseCardPadding = verseCardPadding,
                        bismillahFontSize = bismillahFontSize,
                        onAyahClick = { viewModel.selectAyah(it) },
                        onAyahLongClick = { ayahNum ->
                            selectedMenuAyahNum = ayahNum
                            showContextMenu = true
                        },
                        onJumpClick = { showJumpBottomSheet = true },
                    )
                }
            }
        } else {
            val pagerState = rememberPagerState(
                initialPage = allSurahs.indexOfFirst { it.number == surah.number }.coerceAtLeast(0),
                pageCount = { allSurahs.size },
            )

            LaunchedEffect(surah.number, allSurahs) {
                val targetPage = allSurahs.indexOfFirst { it.number == surah.number }
                if (targetPage >= 0 &&
                    targetPage != pagerState.settledPage &&
                    !pagerState.isScrollInProgress
                ) {
                    pagerState.animateScrollToPage(targetPage)
                }
            }

            LaunchedEffect(pagerState, allSurahs) {
                snapshotFlow { pagerState.currentPage }
                    .distinctUntilChanged()
                    .collect { page ->
                        allSurahs.getOrNull(page)?.number?.let { surahNumber ->
                            viewModel.prefetchAdjacentAyahs(surahNumber)
                        }
                    }
            }

            LaunchedEffect(pagerState, allSurahs) {
                snapshotFlow { pagerState.settledPage }
                    .distinctUntilChanged()
                    .collect { page ->
                        allSurahs.getOrNull(page)?.let { selectedSurah ->
                            if (selectedSurah.number != surah.number) {
                                viewModel.selectSurah(selectedSurah, 1)
                            }
                        }
                    }
            }

            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) { page ->
                val pageSurah = allSurahs[page]
                val pageAyahsState = ayahCache[pageSurah.number] ?: UiState.Loading

                LaunchedEffect(pageSurah.number) {
                    viewModel.ensureAyahsLoaded(pageSurah.number)
                }

                LoadingErrorContent(
                    state = pageAyahsState,
                    onRetry = { viewModel.ensureAyahsLoaded(pageSurah.number) },
                ) { loadedAyahs ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        ReaderAyahContent(
                            layoutMode = readerLayoutMode,
                            surahNumber = pageSurah.number,
                            surahNameArabic = pageSurah.nameArabic,
                            ayahs = loadedAyahs,
                            activeAyahNumber = if (pageSurah.number == surah.number) {
                                activeAyahNumber
                            } else {
                                1
                            },
                            fontSizeProgress = fontSizeProgress,
                            showTranslation = showTranslation,
                            showTransliteration = showTransliteration,
                            horizontalPadding = horizontalPadding,
                            contentMaxWidthDp = windowLayout.contentMaxWidthDp,
                            isCompactWidth = windowLayout.isCompactWidth,
                            verseCardPadding = verseCardPadding,
                            bismillahFontSize = bismillahFontSize,
                            onAyahClick = {
                                if (pageSurah.number != surah.number) {
                                    viewModel.selectSurah(pageSurah, it)
                                } else {
                                    viewModel.selectAyah(it)
                                }
                            },
                            onAyahLongClick = { ayahNum ->
                                if (pageSurah.number != surah.number) {
                                    viewModel.selectSurah(pageSurah, ayahNum)
                                }
                                selectedMenuAyahNum = ayahNum
                                showContextMenu = true
                            },
                            onJumpClick = { showJumpBottomSheet = true },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReaderAyahContent(
    layoutMode: ReaderLayoutMode,
    surahNumber: Int,
    surahNameArabic: String,
    ayahs: List<Ayah>,
    activeAyahNumber: Int,
    fontSizeProgress: Float,
    showTranslation: Boolean,
    showTransliteration: Boolean,
    horizontalPadding: androidx.compose.ui.unit.Dp,
    contentMaxWidthDp: Int,
    isCompactWidth: Boolean,
    verseCardPadding: androidx.compose.ui.unit.Dp,
    bismillahFontSize: androidx.compose.ui.unit.TextUnit,
    onAyahClick: (Int) -> Unit,
    onAyahLongClick: (Int) -> Unit,
    onJumpClick: () -> Unit,
) {
    when (layoutMode) {
        ReaderLayoutMode.List -> ReaderAyahList(
            surahNumber = surahNumber,
            ayahs = ayahs,
            activeAyahNumber = activeAyahNumber,
            fontSizeProgress = fontSizeProgress,
            showTranslation = showTranslation,
            showTransliteration = showTransliteration,
            horizontalPadding = horizontalPadding,
            contentMaxWidthDp = contentMaxWidthDp,
            isCompactWidth = isCompactWidth,
            verseCardPadding = verseCardPadding,
            bismillahFontSize = bismillahFontSize,
            onAyahClick = onAyahClick,
            onAyahLongClick = onAyahLongClick,
            onJumpClick = onJumpClick,
        )
        ReaderLayoutMode.Book -> ReaderBookAyahList(
            surahNumber = surahNumber,
            surahNameArabic = surahNameArabic,
            ayahs = ayahs,
            activeAyahNumber = activeAyahNumber,
            fontSizeProgress = fontSizeProgress,
            showTranslation = showTranslation,
            isDarkTheme = isSystemInDarkTheme(),
            horizontalPadding = horizontalPadding,
            contentMaxWidthDp = contentMaxWidthDp,
            isCompactWidth = isCompactWidth,
            onAyahClick = onAyahClick,
            onAyahLongClick = onAyahLongClick,
            onJumpClick = onJumpClick,
        )
    }
}

@Composable
private fun ReaderAyahList(
    surahNumber: Int,
    ayahs: List<Ayah>,
    activeAyahNumber: Int,
    fontSizeProgress: Float,
    showTranslation: Boolean,
    showTransliteration: Boolean,
    horizontalPadding: androidx.compose.ui.unit.Dp,
    contentMaxWidthDp: Int,
    isCompactWidth: Boolean,
    verseCardPadding: androidx.compose.ui.unit.Dp,
    bismillahFontSize: androidx.compose.ui.unit.TextUnit,
    onAyahClick: (Int) -> Unit,
    onAyahLongClick: (Int) -> Unit,
    onJumpClick: () -> Unit,
) {
    val listModifier = Modifier
        .fillMaxSize()
        .then(
            if (!isCompactWidth) {
                Modifier.widthIn(max = contentMaxWidthDp.dp)
            } else {
                Modifier
            },
        )
        .padding(horizontal = horizontalPadding)

    LazyColumn(
        modifier = listModifier,
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
    ) {
        if (surahNumber != 9) {
            item {
                ReaderBismillahHeader(
                    bismillahFontSize = bismillahFontSize,
                    modifier = Modifier.padding(bottom = 24.dp),
                )
            }
        }

        items(ayahs) { ayah ->
            val isActive = ayah.number == activeAyahNumber
            val fontMultiplier = remember(fontSizeProgress) { 0.6f + (fontSizeProgress / 100f) * 0.8f }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .combinedClickable(
                        onClick = { onAyahClick(ayah.number) },
                        onLongClick = { onAyahLongClick(ayah.number) },
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isActive) {
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f)
                    } else {
                        Color.Transparent
                    },
                ),
                border = if (isActive) {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                } else {
                    null
                },
            ) {
                Row(
                    modifier = Modifier.padding(verseCardPadding),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActive) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.surfaceContainer,
                            ),
                    ) {
                        Text(
                            text = ayah.number.toString(),
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) Color.White else MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                    ) {
                        Text(
                            text = ayah.textArabic,
                            style = QuranVerseLg,
                            fontSize = (34 * fontMultiplier).sp,
                            lineHeight = (68 * fontMultiplier).sp,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        if (showTransliteration && ayah.transliteration.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ayah.transliteration,
                                style = BodyMdCustom,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Left,
                            )
                        }

                        if (showTranslation) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = ayah.textEnglish,
                                style = BodyLg,
                                color = if (isActive) {
                                    MaterialTheme.colorScheme.secondary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Left,
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onJumpClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Default.Pin, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Jump or Select Reciter Setup",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
