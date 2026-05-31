package com.example.ui.tafseer

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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Surah
import com.example.ui.common.UiState
import com.example.ui.components.LoadingErrorContent
import com.example.ui.theme.*
import com.example.ui.navigation.LocalAppNavigator
import com.example.ui.navigation.ReaderKey
import com.example.viewmodel.QuranViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TafseerScreen(viewModel: QuranViewModel) {
    val navigator = LocalAppNavigator.current
    val surahNum by viewModel.tafseerSurahNumber.collectAsState()
    val ayahNum by viewModel.tafseerAyahNumber.collectAsState()
    val activeSource by viewModel.selectedTafseerSource.collectAsState()
    val context = LocalContext.current

    val tafseerTextState by viewModel.tafseerText.collectAsState()
    val currentAyahsState by viewModel.currentAyahs.collectAsState()
    val scope = rememberCoroutineScope()
    var currentSurah by remember { mutableStateOf<Surah?>(null) }

    LaunchedEffect(surahNum) {
        currentSurah = viewModel.findSurah(surahNum)
    }
    LaunchedEffect(surahNum, ayahNum, activeSource) {
        viewModel.loadTafseer()
    }

    val activeAyah = when (val state = currentAyahsState) {
        is UiState.Success -> state.data.find { it.number == ayahNum }
        else -> null
    }
    val surahDisplay = currentSurah

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("تفسير", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
                        Text("${surahDisplay?.nameEnglish ?: ""} • $surahNum:$ayahNum", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
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
                        text = activeAyah?.textArabic ?: "",
                        style = QuranVerseSm,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = activeAyah?.textEnglish ?: "",
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

                    LoadingErrorContent(
                        state = tafseerTextState,
                        onRetry = { viewModel.loadTafseer() },
                    ) { commentary ->
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
                                scope.launch {
                                    viewModel.findSurah(targetS)?.let { sObj ->
                                        viewModel.selectSurah(sObj, if (number == 164) 164 else 2)
                                    }
                                }
                                navigator.pop()
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
