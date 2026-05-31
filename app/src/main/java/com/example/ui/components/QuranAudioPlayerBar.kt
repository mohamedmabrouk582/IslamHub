package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.Reciter
import com.example.data.model.Surah
import com.example.ui.theme.LabelSmCustom
import com.example.ui.theme.TitleMd

enum class QuranPlayerBarVariant {
    Full,
    Compact,
}

object QuranFloatingPlayerInsets {
    val playerHeight = 64.dp
    private val bottomGap = 12.dp

    @Composable
    fun bottomClearance(showAppNavigationBar: Boolean): Dp {
        val systemNavBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val insetBottom = if (showAppNavigationBar) 0.dp else systemNavBottom
        return playerHeight + bottomGap + insetBottom
    }
}

private val PlayerPillHeight = QuranFloatingPlayerInsets.playerHeight
private val PlayerAvatarSize = 48.dp
private val CollapsedPillWidth = 64.dp

@Composable
fun QuranFloatingPlayer(
    surah: Surah,
    reciter: Reciter,
    activeAyahNumber: Int,
    isPlaying: Boolean,
    versePlayProgress: Float?,
    isExpanded: Boolean,
    isDownloadingAudio: Boolean,
    surahDownloadProgress: Float?,
    variant: QuranPlayerBarVariant,
    showAppNavigationBar: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onOpenDetail: () -> Unit,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val horizontalMargin = if (variant == QuranPlayerBarVariant.Compact) 16.dp else 20.dp
    val layoutDirection = LocalLayoutDirection.current
    val safeDrawingPadding = WindowInsets.safeDrawing.asPaddingValues()
    val safeStart = safeDrawingPadding.calculateStartPadding(layoutDirection)
    val safeEnd = safeDrawingPadding.calculateEndPadding(layoutDirection)
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val expandedWidth = (
        screenWidth - horizontalMargin * 2 - safeStart - safeEnd
        ).coerceAtMost(448.dp)
    val animatedWidthFraction by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "playerWidthFraction",
    )
    val pillWidth = CollapsedPillWidth + (expandedWidth - CollapsedPillWidth) * animatedWidthFraction
    val progress = (versePlayProgress ?: 0f).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "avatarRingProgress",
    )
    val showProgressRing = isPlaying || progress > 0f
    val avatarProgress = if (showProgressRing) animatedProgress else null

    Box(
        modifier = modifier
            .fillMaxWidth()
            .floatingPlayerWindowInsets(showAppNavigationBar = showAppNavigationBar)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
            .padding(horizontal = horizontalMargin),
        contentAlignment = if (isExpanded) Alignment.BottomCenter else Alignment.BottomEnd,
    ) {
        Surface(
            shape = if (isExpanded) RoundedCornerShape(999.dp) else CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f),
            shadowElevation = 8.dp,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
            ),
            modifier = Modifier
                .width(pillWidth)
                .height(PlayerPillHeight)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = !isExpanded,
                    onClick = { onExpandedChange(true) },
                ),
            content = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = if (isExpanded) Arrangement.Start else Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (isExpanded) 8.dp else 0.dp),
                ) {
                    Box(
                        modifier = Modifier.size(PlayerAvatarSize),
                        contentAlignment = Alignment.Center,
                    ) {
                        ReciterAvatar(
                            reciter = reciter,
                            size = PlayerAvatarSize,
                            shape = CircleShape,
                            playbackProgress = avatarProgress,
                            rotateWhenPlaying = true,
                            isPlaying = isPlaying,
                        )
                    }

                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = fadeIn(tween(200, delayMillis = 100)) + scaleIn(
                            initialScale = 0.92f,
                            animationSpec = tween(250, delayMillis = 100),
                        ),
                        exit = fadeOut(tween(150)) + scaleOut(targetScale = 0.92f, animationSpec = tween(150)),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .widthIn(min = 0.dp)
                                    .clickable(onClick = onOpenDetail),
                            ) {
                                Text(
                                    text = "${surah.nameEnglish} • Verse $activeAyahNumber",
                                    style = TitleMd,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.basicMarquee()
                                )
                                Text(
                                    text = reciter.nameEnglish,
                                    style = LabelSmCustom,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.basicMarquee()
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(start = 12.dp, end = 4.dp),
                            ) {
                                IconButton(
                                    onClick = onPrevious,
                                    modifier = Modifier.size(36.dp),
                                    colors = IconButtonDefaults.iconButtonColors(
                                        contentColor = MaterialTheme.colorScheme.primary,
                                    ),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SkipPrevious,
                                        contentDescription = "Previous Verse",
                                        modifier = Modifier.size(22.dp),
                                    )
                                }

                                PlayerPlayPauseButton(
                                    isPlaying = isPlaying,
                                    onClick = onPlayPause,
                                    size = 40.dp,
                                    iconSize = 24.dp,
                                )

                                IconButton(
                                    onClick = onNext,
                                    modifier = Modifier.size(36.dp),
                                    colors = IconButtonDefaults.iconButtonColors(
                                        contentColor = MaterialTheme.colorScheme.primary,
                                    ),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SkipNext,
                                        contentDescription = "Next Verse",
                                        modifier = Modifier.size(22.dp),
                                    )
                                }



                                IconButton(
                                    onClick = { onExpandedChange(false) },
                                    modifier = Modifier.size(36.dp),
                                    colors = IconButtonDefaults.iconButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    ),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Collapse player",
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            },
        )
    }
}

@Composable
private fun Modifier.floatingPlayerWindowInsets(showAppNavigationBar: Boolean): Modifier {
    return if (showAppNavigationBar) {
        padding(bottom = 12.dp)
    } else {
        navigationBarsPadding().padding(bottom = 12.dp)
    }
}

@Composable
private fun PlayerPlayPauseButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp,
    iconSize: Dp,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
            modifier = Modifier.size(iconSize),
        )
    }
}
