package com.example.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.QuranData
import com.example.data.model.Ayah
import com.example.data.model.Reciter
import com.example.data.model.Surah
import com.example.ui.adaptive.WindowLayout
import com.example.ui.components.ReciterAvatar
import com.example.ui.components.TwoLineMarqueeText
import com.example.ui.theme.BodyLg
import com.example.ui.theme.BodyMdCustom
import com.example.ui.theme.DisplayArabic
import com.example.ui.theme.HeadlineLgMobile
import com.example.ui.theme.LabelSmCustom
import com.example.ui.theme.TitleMd
import com.example.viewmodel.QuranViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReaderAudioPlayerDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    windowLayout: WindowLayout,
    viewModel: QuranViewModel,
    surah: Surah,
    reciter: Reciter,
    activeAyah: Ayah?,
    activeAyahNumber: Int,
    showTranslation: Boolean,
    isPlaying: Boolean,
    playbackProgress: Float?,
    resolveSurah: (Int) -> Surah?,
) {
    if (!visible) return

    val configuration = LocalConfiguration.current
    val isLargeLayout = !windowLayout.isCompactWidth
    val popularReciters = QuranData.popularReciters
    val dialogWidthFraction = if (isLargeLayout) 0.95f else 0.92f
    val maxDialogHeight = (configuration.screenHeightDp * 0.9f).dp

    if (isLargeLayout) {
        ReaderAudioPlayerCenteredDialog(
            onDismiss = onDismiss,
            dialogWidthFraction = dialogWidthFraction,
            maxDialogHeight = maxDialogHeight,
            activeAyah = activeAyah,
            activeAyahNumber = activeAyahNumber,
            showTranslation = showTranslation,
            surah = surah,
            reciter = reciter,
            isPlaying = isPlaying,
            playbackProgress = playbackProgress,
            popularReciters = popularReciters,
            viewModel = viewModel,
            resolveSurah = resolveSurah,
        )
    } else {
        ReaderAudioPlayerBottomSheet(
            onDismiss = onDismiss,
            activeAyah = activeAyah,
            activeAyahNumber = activeAyahNumber,
            showTranslation = showTranslation,
            surah = surah,
            reciter = reciter,
            isPlaying = isPlaying,
            playbackProgress = playbackProgress,
            popularReciters = popularReciters,
            viewModel = viewModel,
            resolveSurah = resolveSurah,
        )
    }
}

@Composable
private fun ReaderAudioPlayerCenteredDialog(
    onDismiss: () -> Unit,
    dialogWidthFraction: Float,
    maxDialogHeight: androidx.compose.ui.unit.Dp,
    activeAyah: Ayah?,
    activeAyahNumber: Int,
    showTranslation: Boolean,
    surah: Surah,
    reciter: Reciter,
    isPlaying: Boolean,
    playbackProgress: Float?,
    popularReciters: List<Reciter>,
    viewModel: QuranViewModel,
    resolveSurah: (Int) -> Surah?,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    .clickable(onClick = onDismiss),
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth(dialogWidthFraction)
                    .wrapContentHeight()
                    .heightIn(max = maxDialogHeight)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shadowElevation = 16.dp,
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .padding(top = 48.dp, bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                        ) {
                            DialogAyahScrollCard(
                                activeAyah = activeAyah,
                                activeAyahNumber = activeAyahNumber,
                                showTranslation = showTranslation,
                                isLargeLayout = true,
                                modifier = Modifier
                                    .weight(0.55f)
                                    .fillMaxHeight(),
                            )
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
                            )
                            DialogPlayerSectionLarge(
                                reciter = reciter,
                                surah = surah,
                                activeAyahNumber = activeAyahNumber,
                                isPlaying = isPlaying,
                                playbackProgress = playbackProgress,
                                popularReciters = popularReciters,
                                viewModel = viewModel,
                                resolveSurah = resolveSurah,
                                modifier = Modifier.weight(0.45f),
                            )
                        }

                        DialogStatusFooter(reciter = reciter)
                    }

                    DialogCloseButton(
                        onDismiss = onDismiss,
                        modifier = Modifier.align(Alignment.TopEnd),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderAudioPlayerBottomSheet(
    onDismiss: () -> Unit,
    activeAyah: Ayah?,
    activeAyahNumber: Int,
    showTranslation: Boolean,
    surah: Surah,
    reciter: Reciter,
    isPlaying: Boolean,
    playbackProgress: Float?,
    popularReciters: List<Reciter>,
    viewModel: QuranViewModel,
    resolveSurah: (Int) -> Surah?,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 8.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            ) {
                DialogAyahScrollCard(
                    activeAyah = activeAyah,
                    activeAyahNumber = activeAyahNumber,
                    showTranslation = showTranslation,
                    isLargeLayout = false,
                    modifier = Modifier.fillMaxWidth(),
                )
                DialogCloseButton(
                    onDismiss = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 12.dp, bottom = 8.dp),
            ) {
                DialogPlayerSectionSmall(
                    reciter = reciter,
                    surah = surah,
                    activeAyahNumber = activeAyahNumber,
                    isPlaying = isPlaying,
                    playbackProgress = playbackProgress,
                    popularReciters = popularReciters,
                    viewModel = viewModel,
                    resolveSurah = resolveSurah,
                )
            }

            DialogStatusFooter(reciter = reciter)
        }
    }
}

@Composable
private fun DialogCloseButton(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onDismiss,
        modifier = modifier.padding(4.dp),
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(8.dp)
                    .size(24.dp),
            )
        }
    }
}

@Composable
private fun DialogAyahScrollCard(
    activeAyah: Ayah?,
    activeAyahNumber: Int,
    showTranslation: Boolean,
    isLargeLayout: Boolean,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val arabicFontSize = if (isLargeLayout) 48.sp else 32.sp
    val arabicLineHeight = if (isLargeLayout) 76.sp else 58.sp

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isLargeLayout) {
                        Modifier
                            .fillMaxHeight()
                            .verticalScroll(scrollState)
                    } else {
                        Modifier
                    },
                )
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (isLargeLayout) 24.dp else 16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(if (isLargeLayout) 40.dp else 32.dp)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = activeAyahNumber.toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isLargeLayout) 14.sp else 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            if (isLargeLayout) {
                Text(
                    text = activeAyah?.textArabic ?: "…",
                    style = DisplayArabic.copy(color = MaterialTheme.colorScheme.onSurface),
                    fontSize = arabicFontSize,
                    lineHeight = arabicLineHeight,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                if (showTranslation) {
                    val translation = activeAyah?.textEnglish
                    if (!translation.isNullOrEmpty()) {
                        Text(
                            text = translation,
                            style = BodyLg.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            } else {
                TwoLineMarqueeText(
                    text = activeAyah?.textArabic ?: "…",
                    style = DisplayArabic.copy(color = MaterialTheme.colorScheme.onSurface),
                    fontSize = arabicFontSize,
                    lineHeight = arabicLineHeight,
                    textAlign = TextAlign.Center,
                )

                if (showTranslation) {
                    val translation = activeAyah?.textEnglish
                    if (!translation.isNullOrEmpty()) {
                        TwoLineMarqueeText(
                            text = translation,
                            style = BodyMdCustom.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogReciterHeader(
    reciter: Reciter,
    isLargeLayout: Boolean,
    playbackProgress: Float?,
) {
    val avatarSize = if (isLargeLayout) 112.dp else 80.dp
    val avatarShape = if (isLargeLayout) RoundedCornerShape(24.dp) else CircleShape

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {

            ReciterAvatar(
                reciter = reciter,
                size = avatarSize,
                shape = avatarShape,
                playbackProgress = playbackProgress,
            )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = reciter.nameEnglish,
                style = if (isLargeLayout) HeadlineLgMobile else TitleMd,
                fontSize = if (isLargeLayout) 22.sp else 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = (reciter.nameArabic ?: reciter.bitrateLabel).uppercase(),
                style = LabelSmCustom,
                color = if (isLargeLayout) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                },
                letterSpacing = if (isLargeLayout) 2.sp else 1.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun DialogPlayerSectionLarge(
    reciter: Reciter,
    surah: Surah,
    activeAyahNumber: Int,
    isPlaying: Boolean,
    playbackProgress: Float?,
    popularReciters: List<Reciter>,
    viewModel: QuranViewModel,
    resolveSurah: (Int) -> Surah?,
    modifier: Modifier = Modifier,
) {
    val headerProgress = playbackProgress

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        DialogReciterHeader(
            reciter = reciter,
            isLargeLayout = true,
            playbackProgress = headerProgress,
        )

        DialogPlaybackControls(
            isPlaying = isPlaying,
            fabSize = 72.dp,
            playIconSize = 40.dp,
            controlIconSize = 28.dp,
            onTogglePlay = { viewModel.setPlaying(!isPlaying) },
            onNextAyah = {
                if (activeAyahNumber < surah.versesCount) viewModel.selectAyah(activeAyahNumber + 1)
            },
            onPrevAyah = {
                if (activeAyahNumber > 1) viewModel.selectAyah(activeAyahNumber - 1)
            },
        )

        DialogReciterSwitchSection(
            popularReciters = popularReciters,
            selectedReciter = reciter,
            onReciterSelected = viewModel::selectReciter,
            compact = false,
            playbackProgress = playbackProgress,
            isPlaying = isPlaying,
        )
    }
}

@Composable
private fun DialogPlayerSectionSmall(
    reciter: Reciter,
    surah: Surah,
    activeAyahNumber: Int,
    isPlaying: Boolean,
    playbackProgress: Float?,
    popularReciters: List<Reciter>,
    viewModel: QuranViewModel,
    resolveSurah: (Int) -> Surah?,
    modifier: Modifier = Modifier,
) {
    val headerProgress = playbackProgress

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(unbounded = false)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            DialogReciterHeader(
                reciter = reciter,
                isLargeLayout = false,
                playbackProgress = headerProgress,
            )

            DialogPlaybackControls(
                isPlaying = isPlaying,
                fabSize = 56.dp,
                playIconSize = 30.dp,
                controlIconSize = 24.dp,
                onTogglePlay = { viewModel.setPlaying(!isPlaying) },
                onNextAyah = {
                    if (activeAyahNumber < surah.versesCount) viewModel.selectAyah(activeAyahNumber + 1)
                },
                onPrevAyah = {
                    if (activeAyahNumber > 1) viewModel.selectAyah(activeAyahNumber - 1)
                },
            )

            DialogReciterSwitchSection(
                popularReciters = popularReciters,
                selectedReciter = reciter,
                onReciterSelected = viewModel::selectReciter,
                compact = true,
                playbackProgress = playbackProgress,
                isPlaying = isPlaying,
            )
        }
    }
}

@Composable
private fun DialogPlaybackControls(
    isPlaying: Boolean,
    fabSize: androidx.compose.ui.unit.Dp,
    playIconSize: androidx.compose.ui.unit.Dp,
    controlIconSize: androidx.compose.ui.unit.Dp,
    onTogglePlay: () -> Unit,
    onNextAyah: () -> Unit,
    onPrevAyah: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        IconButton(onClick = onPrevAyah) {
            Icon(Icons.Default.SkipPrevious, contentDescription = "Previous Verse", modifier = Modifier.size(controlIconSize))
        }
        Spacer(modifier = Modifier.width(8.dp))
        FloatingActionButton(
            onClick = onTogglePlay,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.size(fabSize),
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = "Toggle playback",
                modifier = Modifier.size(playIconSize),
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onNextAyah) {
            Icon(Icons.Default.SkipNext, contentDescription = "Next Verse", modifier = Modifier.size(controlIconSize))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DialogReciterSwitchSection(
    popularReciters: List<Reciter>,
    selectedReciter: Reciter,
    onReciterSelected: (Reciter) -> Unit,
    compact: Boolean,
    playbackProgress: Float?,
    isPlaying: Boolean,
) {
    val chipProgress = playbackProgress

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (!compact) {
                    Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                } else {
                    Modifier
                },
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "SWITCH RECITER",
            style = LabelSmCustom,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        if (compact) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.Top,
            ) {
                popularReciters.take(6).forEach { r ->
                    DialogReciterChip(
                        reciter = r,
                        selected = selectedReciter.id == r.id,
                        compact = true,
                        playbackProgress = if (selectedReciter.id == r.id) chipProgress else null,
                        onClick = { onReciterSelected(r) },
                    )
                }
            }
        } else {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                popularReciters.take(6).forEach { r ->
                    DialogReciterChip(
                        reciter = r,
                        selected = selectedReciter.id == r.id,
                        compact = false,
                        playbackProgress = if (selectedReciter.id == r.id) chipProgress else null,
                        onClick = { onReciterSelected(r) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DialogReciterChip(
    reciter: Reciter,
    selected: Boolean,
    compact: Boolean,
    playbackProgress: Float?,
    onClick: () -> Unit,
) {
    val displayName = reciter.nameEnglish.substringBefore(" ").let { first ->
        if (reciter.nameEnglish.length > 18) first else reciter.nameEnglish
    }
    val avatarSize = if (compact) 40.dp else 32.dp

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(if (compact) 12.dp else 12.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant,
        ),
        shadowElevation = if (selected) 4.dp else 0.dp,
    ) {
        if (compact) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .width(72.dp)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
            ) {
                ReciterAvatar(
                    reciter = reciter,
                    size = avatarSize,
                    shape = CircleShape,
                    playbackProgress = playbackProgress,
                )
                Text(
                    text = displayName,
                    fontSize = 11.sp,
                    style = TitleMd,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 8.dp,
                ),
            ) {
                ReciterAvatar(
                    reciter = reciter,
                    size = avatarSize,
                    shape = CircleShape,
                    playbackProgress = playbackProgress,
                )
                Text(
                    text = displayName,
                    fontSize = 14.sp,
                    style = TitleMd,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DialogStatusFooter(reciter: Reciter) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f))
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "High Quality • ${reciter.bitrateLabel}",
            style = LabelSmCustom,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        )
        Icon(
            Icons.Default.VerifiedUser,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
    }
}
