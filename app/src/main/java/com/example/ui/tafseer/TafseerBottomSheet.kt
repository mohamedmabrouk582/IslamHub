package com.example.ui.tafseer

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Surah
import com.example.ui.common.UiState
import com.example.ui.components.LoadingErrorContent
import com.example.ui.theme.BodyMdCustom
import com.example.ui.theme.QuranVerseLg
import com.example.ui.theme.QuranVerseSm
import com.example.ui.theme.TitleMd
import com.example.viewmodel.QuranViewModel
import kotlinx.coroutines.launch

private val tafseerSources = listOf(
    "التفسير الميسر",
    "ابن كثير",
    "الطبري",
    "السعدي",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TafseerBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    viewModel: QuranViewModel,
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        TafseerSheetContent(
            viewModel = viewModel,
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun TafseerSheetContent(
    viewModel: QuranViewModel,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val surahNum by viewModel.tafseerSurahNumber.collectAsState()
    val ayahNum by viewModel.tafseerAyahNumber.collectAsState()
    val activeSource by viewModel.selectedTafseerSource.collectAsState()
    val tafseerTextState by viewModel.tafseerText.collectAsState()
    val ayahCache by viewModel.ayahCache.collectAsState()
    var currentSurah by remember { mutableStateOf<Surah?>(null) }

    LaunchedEffect(surahNum) {
        currentSurah = viewModel.findSurah(surahNum)
        viewModel.ensureAyahsLoaded(surahNum)
    }
    LaunchedEffect(surahNum, ayahNum, activeSource) {
        viewModel.loadTafseer()
    }

    val activeAyah = when (val state = ayahCache[surahNum]) {
        is UiState.Success -> state.data.find { it.number == ayahNum }
        else -> null
    }
    val surahDisplay = currentSurah
    val maxSheetHeight = LocalConfiguration.current.screenHeightDp.dp * 0.92f

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = maxSheetHeight)
            .navigationBarsPadding()
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "تفسير",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 20.sp,
                )
                Text(
                    text = "${surahDisplay?.nameEnglish ?: ""} • $surahNum:$ayahNum",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = { Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show() }) {
                Icon(Icons.Default.BookmarkBorder, contentDescription = "Bookmark")
            }
            IconButton(onClick = { Toast.makeText(context, "Shared!", Toast.LENGTH_SHORT).show() }) {
                Icon(Icons.Default.Share, contentDescription = "Share")
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
        }

        item {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = activeAyah?.textArabic ?: "",
                    style = QuranVerseSm,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = activeAyah?.textEnglish ?: "",
                    style = BodyMdCustom,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Left,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        }

        item {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        ) {
            items(tafseerSources) { source ->
                val isSelected = activeSource == source
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setTafseerSource(source) },
                    label = { Text(source) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        }
        }

        item {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                    ) {
                        Icon(
                            Icons.Default.Book,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "تفسير $activeSource",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        style = TitleMd,
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
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        }

        item {
        Text(
            text = "آيات ذات صلة",
            style = TitleMd,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        }

        item {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        ) {
            listOf(
                Triple(164, "سورة البقرة", "إِنَّ فِي خَلْقِ السَّمَاوَاتِ..."),
                Triple(2, "سورة آل عمران", "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ..."),
            ).forEach { (number, surahName, arabic) ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            val targetSurah = if (number == 164) 2 else 3
                            scope.launch {
                                viewModel.findSurah(targetSurah)?.let { surah ->
                                    viewModel.selectSurah(surah, if (number == 164) 164 else 2)
                                }
                            }
                            onDismiss()
                        },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp),
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary),
                        ) {
                            Text(
                                number.toString(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                arabic,
                                style = QuranVerseLg,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(surahName, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        }
    }
}
