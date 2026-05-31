package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Reciter
import com.example.data.model.Surah

enum class QuranPlayerBarVariant {
    Full,
    Compact,
}

@Composable
fun QuranAudioPlayerBar(
    surah: Surah,
    reciter: Reciter,
    activeAyahNumber: Int,
    versesCount: Int,
    isPlaying: Boolean,
    playProgress: Float,
    isDownloadingAudio: Boolean,
    surahDownloadProgress: Float?,
    variant: QuranPlayerBarVariant,
    onBarClick: () -> Unit,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val avatarSize = if (variant == QuranPlayerBarVariant.Compact) 36.dp else 44.dp
    val rowVerticalPadding = if (variant == QuranPlayerBarVariant.Compact) 8.dp else 12.dp
    val playButtonPadding = if (variant == QuranPlayerBarVariant.Compact) 6.dp else 8.dp

    Surface(
        tonalElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column {
            if (isDownloadingAudio && variant == QuranPlayerBarVariant.Full) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                    )
                    Text(
                        text = stringResource(R.string.reader_downloading_audio),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    surahDownloadProgress?.let { progress ->
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            LinearProgressIndicator(
                progress = { playProgress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
            )

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onBarClick)
                    .padding(horizontal = 16.dp, vertical = rowVerticalPadding),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    ReciterAvatar(
                        reciter = reciter,
                        size = avatarSize,
                        shape = RoundedCornerShape(8.dp),
                    )
                    Spacer(modifier = Modifier.width(if (variant == QuranPlayerBarVariant.Compact) 10.dp else 12.dp))
                    Column {
                        Text(
                            text = reciter.nameEnglish,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            fontSize = if (variant == QuranPlayerBarVariant.Compact) 13.sp else 14.sp,
                        )
                        Text(
                            text = "${surah.nameEnglish} • Verse $activeAyahNumber",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPrevious) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Previous Verse")
                    }

                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(playButtonPadding),
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Toggle",
                            tint = Color.White,
                        )
                    }

                    IconButton(onClick = onNext) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Next Verse")
                    }
                }
            }
        }
    }
}
