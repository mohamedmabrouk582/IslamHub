package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.Reciter

@Composable
fun ReciterAvatar(
    reciter: Reciter,
    size: Dp,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    showRing: Boolean = false,
    playbackProgress: Float? = null,
) {
    val progressRingStroke = 3.dp
    val avatarInset = if (playbackProgress != null) progressRingStroke + 1.dp else 0.dp

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(reciter.avatarResId),
            contentDescription = reciter.nameEnglish,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .padding(avatarInset)
                .clip(shape),
        )
        if (playbackProgress != null) {
            CircularProgressIndicator(
                progress = { playbackProgress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = progressRingStroke,
                trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                color = MaterialTheme.colorScheme.primary,
                strokeCap = StrokeCap.Round,
            )
        }
        if (showRing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(avatarInset)
                    .border(3.dp, MaterialTheme.colorScheme.surface, shape),
            )
        }
    }
}
