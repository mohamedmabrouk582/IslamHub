package com.example.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.QuranData
import com.example.data.model.Reciter
import com.example.data.model.Surah
import com.example.ui.adaptive.WindowLayout
import com.example.ui.components.ReciterAvatar
import com.example.ui.theme.BodyMdCustom
import com.example.ui.theme.DisplayArabic
import com.example.ui.theme.LabelSmCustom
import com.example.ui.theme.PlayerBackground
import com.example.ui.theme.TitleMd
import com.example.viewmodel.QuranViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderAudioPlayerDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onStartFromVerse: () -> Unit,
    windowLayout: WindowLayout,
    viewModel: QuranViewModel,
    surah: Surah,
    reciter: Reciter,
    activeAyahNumber: Int,
    isPlaying: Boolean,
    playProgress: Float,
    resolveSurah: (Int) -> Surah?,
) {
    if (!visible) return

    if (windowLayout.isCompactWidth) {
        ReaderAudioPlayerBottomSheet(
            onDismiss = onDismiss,
            viewModel = viewModel,
            surah = surah,
            reciter = reciter,
            activeAyahNumber = activeAyahNumber,
            isPlaying = isPlaying,
            playProgress = playProgress,
            resolveSurah = resolveSurah,
        )
    } else {
        ReaderAudioPlayerExpandedOverlay(
            onDismiss = onDismiss,
            onStartFromVerse = onStartFromVerse,
            viewModel = viewModel,
            surah = surah,
            reciter = reciter,
            activeAyahNumber = activeAyahNumber,
            isPlaying = isPlaying,
            playProgress = playProgress,
            resolveSurah = resolveSurah,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderAudioPlayerBottomSheet(
    onDismiss: () -> Unit,
    viewModel: QuranViewModel,
    surah: Surah,
    reciter: Reciter,
    activeAyahNumber: Int,
    isPlaying: Boolean,
    playProgress: Float,
    resolveSurah: (Int) -> Surah?,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 44.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    surah.nameArabic,
                    style = DisplayArabic,
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    "Playing from verse $activeAyahNumber",
                    style = BodyMdCustom,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            ReciterProgressAvatar(
                reciter = reciter,
                playProgress = playProgress,
                avatarSize = 140.dp,
                ringSize = 160.dp,
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(reciter.nameEnglish, fontWeight = FontWeight.Bold, style = TitleMd)
                reciter.nameArabic?.let { arabicName ->
                    Text(arabicName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(reciter.bitrateLabel, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            AudioWaveformVisualizer(isPlaying = isPlaying, modifier = Modifier.height(40.dp))

            AudioPlaybackControlsRow(
                isPlaying = isPlaying,
                onTogglePlay = { viewModel.setPlaying(!isPlaying) },
                onNextSurah = {
                    val next = surah.number + 1
                    if (next <= 114) resolveSurah(next)?.let { viewModel.selectSurah(it, 1) }
                },
                onPrevSurah = {
                    val prev = surah.number - 1
                    if (prev >= 1) resolveSurah(prev)?.let { viewModel.selectSurah(it, 1) }
                },
                onNextAyah = {
                    val next = activeAyahNumber + 1
                    if (next <= surah.versesCount) viewModel.selectAyah(next)
                },
                onPrevAyah = {
                    val prev = activeAyahNumber - 1
                    if (prev >= 1) viewModel.selectAyah(prev)
                },
                fabSize = 72.dp,
                iconSize = 32.dp,
            )

            val activeSpeed by viewModel.playbackSpeed.collectAsState()
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                        RoundedCornerShape(32.dp),
                    )
                    .padding(8.dp),
            ) {
                PlaybackSpeedSelector(
                    activeSpeed = activeSpeed,
                    onSpeedSelected = viewModel::setPlaybackSpeed,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(
                        Icons.Default.Repeat,
                        contentDescription = "Repeat",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Icon(
                        Icons.Default.Bedtime,
                        contentDescription = "Sleep Timer",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            ReciterListSection(
                selectedReciter = reciter,
                onReciterSelected = viewModel::selectReciter,
                maxListHeight = 320.dp,
            )
        }
    }
}

@Composable
private fun ReaderAudioPlayerExpandedOverlay(
    onDismiss: () -> Unit,
    onStartFromVerse: () -> Unit,
    viewModel: QuranViewModel,
    surah: Surah,
    reciter: Reciter,
    activeAyahNumber: Int,
    isPlaying: Boolean,
    playProgress: Float,
    resolveSurah: (Int) -> Surah?,
) {
    var showAllReciters by remember { mutableStateOf(false) }
    val activeSpeed by viewModel.playbackSpeed.collectAsState()
    val popularReciters = QuranData.popularReciters
    val otherReciters = remember(popularReciters) {
        val popularIds = popularReciters.map { it.id }.toSet()
        QuranData.reciters.filter { it.id !in popularIds }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                    .clickable(onClick = onDismiss),
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp, vertical = 48.dp)
                    .widthIn(max = 1024.dp)
                    .fillMaxWidth(0.92f)
                    .heightIn(max = 720.dp),
                shape = RoundedCornerShape(40.dp),
                color = PlayerBackground,
                shadowElevation = 24.dp,
            ) {
                Box {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp),
                    ) {
                        Icon(
                            Icons.Default.ExpandMore,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp),
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .weight(0.42f)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.3f))
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            ReciterProgressAvatar(
                                reciter = reciter,
                                playProgress = playProgress,
                                avatarSize = 224.dp,
                                ringSize = 256.dp,
                                strokeWidth = 2.5.dp,
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                reciter.nameEnglish,
                                style = TitleMd,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                reciter.nameArabic ?: reciter.bitrateLabel.uppercase(),
                                style = LabelSmCustom,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                letterSpacing = 2.sp,
                                modifier = Modifier.padding(top = 4.dp),
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            AudioWaveformVisualizer(
                                isPlaying = isPlaying,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp),
                                barCount = 8,
                            )

                            Spacer(modifier = Modifier.height(40.dp))

                            AudioPlaybackControlsRow(
                                isPlaying = isPlaying,
                                onTogglePlay = { viewModel.setPlaying(!isPlaying) },
                                onNextSurah = {
                                    val next = surah.number + 1
                                    if (next <= 114) resolveSurah(next)?.let { viewModel.selectSurah(it, 1) }
                                },
                                onPrevSurah = {
                                    val prev = surah.number - 1
                                    if (prev >= 1) resolveSurah(prev)?.let { viewModel.selectSurah(it, 1) }
                                },
                                onNextAyah = {
                                    val next = activeAyahNumber + 1
                                    if (next <= surah.versesCount) viewModel.selectAyah(next)
                                },
                                onPrevAyah = {
                                    val prev = activeAyahNumber - 1
                                    if (prev >= 1) viewModel.selectAyah(prev)
                                },
                                fabSize = 96.dp,
                                iconSize = 36.dp,
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(0.58f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                                .padding(start = 40.dp, end = 56.dp, top = 56.dp, bottom = 40.dp),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 32.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        surah.nameArabic,
                                        style = DisplayArabic,
                                        fontSize = 48.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.padding(top = 8.dp),
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            ) {
                                                Icon(
                                                    Icons.Default.AutoStories,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp),
                                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    "Verse $activeAyahNumber",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                )
                                            }
                                        }
                                        Text(
                                            "${surah.nameEnglish} • ${surah.versesCount} Verses",
                                            style = BodyMdCustom,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }

                                Surface(
                                    onClick = onStartFromVerse,
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                    ) {
                                        Text(
                                            "Start from verse...",
                                            style = TitleMd,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        )
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f)),
                            )

                            Spacer(modifier = Modifier.height(40.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    "SELECT RECITER",
                                    style = LabelSmCustom,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 2.sp,
                                )
                                TextButton(onClick = { showAllReciters = !showAllReciters }) {
                                    Text(
                                        if (showAllReciters) "Show Popular" else "View All",
                                        style = LabelSmCustom,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                popularReciters.forEach { r ->
                                    ReciterChip(
                                        reciter = r,
                                        selected = reciter.id == r.id,
                                        onClick = { viewModel.selectReciter(r) },
                                    )
                                }
                            }

                            if (showAllReciters) {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 240.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    items(otherReciters, key = { it.id }) { r ->
                                        ReciterPickerRow(
                                            reciter = r,
                                            selected = reciter.id == r.id,
                                            onClick = { viewModel.selectReciter(r) },
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(40.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(32.dp),
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "PLAYBACK SPEED",
                                        style = LabelSmCustom,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        letterSpacing = 2.sp,
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    PlaybackSpeedSelector(
                                        activeSpeed = activeSpeed,
                                        onSpeedSelected = viewModel::setPlaybackSpeed,
                                        useSegmentedStyle = true,
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "AUDIO PREFERENCES",
                                        style = LabelSmCustom,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        letterSpacing = 2.sp,
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        AudioPreferenceButton(
                                            icon = Icons.Default.Repeat,
                                            label = "Repeat",
                                        )
                                        AudioPreferenceButton(
                                            icon = Icons.Default.Bedtime,
                                            label = "Night",
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(40.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f)),
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 24.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    "Streaming in High Quality",
                                    style = LabelSmCustom,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Icon(
                                        Icons.Default.Wifi,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    )
                                    Text(
                                        "Online",
                                        style = LabelSmCustom,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
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

@Composable
private fun ReciterProgressAvatar(
    reciter: Reciter,
    playProgress: Float,
    avatarSize: androidx.compose.ui.unit.Dp,
    ringSize: androidx.compose.ui.unit.Dp,
    strokeWidth: androidx.compose.ui.unit.Dp = 4.dp,
) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(ringSize)) {
        CircularProgressIndicator(
            progress = { playProgress },
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = strokeWidth,
            trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
        )
        ReciterAvatar(
            reciter = reciter,
            size = avatarSize,
            showRing = true,
        )
    }
}

@Composable
private fun AudioWaveformVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 8,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.Bottom,
        modifier = modifier.fillMaxWidth(),
    ) {
        val count = if (isPlaying) barCount else barCount / 2
        for (i in 0 until count) {
            val h = if (isPlaying) (12..56).random().dp else 8.dp
            val alpha = if (isPlaying) 0.3f + (i % 4) * 0.15f else 0.3f
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(h)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha)),
            )
        }
    }
}

@Composable
private fun AudioPlaybackControlsRow(
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onNextSurah: () -> Unit,
    onPrevSurah: () -> Unit,
    onNextAyah: () -> Unit,
    onPrevAyah: () -> Unit,
    fabSize: androidx.compose.ui.unit.Dp,
    iconSize: androidx.compose.ui.unit.Dp,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        IconButton(onClick = onNextSurah) {
            Icon(Icons.Default.SkipNext, contentDescription = "Next Surah", modifier = Modifier.size(iconSize))
        }
        IconButton(onClick = onPrevAyah) {
            Icon(Icons.Default.Replay10, contentDescription = "Previous Ayah", modifier = Modifier.size(iconSize))
        }
        FloatingActionButton(
            onClick = onTogglePlay,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.size(fabSize),
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = "Toggle",
                modifier = Modifier.size(iconSize),
            )
        }
        IconButton(onClick = onNextAyah) {
            Icon(Icons.Default.Forward10, contentDescription = "Next Ayah", modifier = Modifier.size(iconSize))
        }
        IconButton(onClick = onPrevSurah) {
            Icon(Icons.Default.SkipPrevious, contentDescription = "Previous Surah", modifier = Modifier.size(iconSize))
        }
    }
}

@Composable
private fun PlaybackSpeedSelector(
    activeSpeed: Float,
    onSpeedSelected: (Float) -> Unit,
    useSegmentedStyle: Boolean = false,
) {
    val speeds = listOf(0.75f, 1.0f, 1.25f, 1.5f)
    if (useSegmentedStyle) {
        Row(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                    RoundedCornerShape(16.dp),
                )
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            speeds.forEach { speed ->
                val selected = activeSpeed == speed
                Surface(
                    onClick = { onSpeedSelected(speed) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) Color.White else Color.Transparent,
                    shadowElevation = if (selected) 2.dp else 0.dp,
                ) {
                    Text(
                        text = "${speed}x",
                        style = LabelSmCustom,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            speeds.forEach { speed ->
                val selected = activeSpeed == speed
                FilterChip(
                    selected = selected,
                    onClick = { onSpeedSelected(speed) },
                    label = { Text("${speed}x") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White,
                    ),
                    shape = RoundedCornerShape(32.dp),
                )
            }
        }
    }
}

@Composable
private fun ReciterChip(
    reciter: Reciter,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant,
        ),
        shadowElevation = if (selected) 8.dp else 0.dp,
    ) {
        Text(
            text = reciter.nameEnglish,
            style = TitleMd,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        )
    }
}

@Composable
private fun AudioPreferenceButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
) {
    Surface(
        onClick = {},
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, style = TitleMd, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ReciterListSection(
    selectedReciter: Reciter,
    onReciterSelected: (Reciter) -> Unit,
    maxListHeight: androidx.compose.ui.unit.Dp,
) {
    val popularReciters = QuranData.popularReciters
    val otherReciters = remember(popularReciters) {
        val popularIds = popularReciters.map { it.id }.toSet()
        QuranData.reciters.filter { it.id !in popularIds }
    }
    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
        Text(
            "SELECT RECITER",
            style = LabelSmCustom,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxListHeight),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item {
                Text(
                    "Popular",
                    style = LabelSmCustom,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                )
            }
            items(popularReciters, key = { it.id }) { r ->
                ReciterPickerRow(
                    reciter = r,
                    selected = selectedReciter.id == r.id,
                    onClick = { onReciterSelected(r) },
                )
            }
            item {
                Text(
                    "All reciters",
                    style = LabelSmCustom,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                )
            }
            items(otherReciters, key = { it.id }) { r ->
                ReciterPickerRow(
                    reciter = r,
                    selected = selectedReciter.id == r.id,
                    onClick = { onReciterSelected(r) },
                )
            }
        }
    }
}

@Composable
internal fun ReciterPickerRow(
    reciter: Reciter,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    } else {
        Color.Transparent
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        ReciterAvatar(
            reciter = reciter,
            size = 40.dp,
            shape = CircleShape,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = reciter.nameEnglish,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Text(
                text = reciter.bitrateLabel,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
