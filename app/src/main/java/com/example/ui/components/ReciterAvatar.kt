package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.example.R
import com.example.data.model.Reciter

@Composable
fun ReciterAvatar(
    reciter: Reciter,
    size: Dp,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    showRing: Boolean = false,
    playbackProgress: Float? = null,
    rotateWhenPlaying: Boolean = false,
    isPlaying: Boolean = false,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val sizePx = with(density) { size.roundToPx().coerceAtLeast(1) }
    val imageRequest = remember(reciter.avatarResId, sizePx) {
        ImageRequest.Builder(context)
            .data(reciter.avatarResId)
            .size(sizePx, sizePx)
            .scale(Scale.FILL)
            .crossfade(120)
            .placeholder(R.drawable.reciter_avatar_fallback)
            .error(R.drawable.reciter_avatar_fallback)
            .build()
    }

    val progressRingStroke = 2.dp
    val avatarInset = if (playbackProgress != null) progressRingStroke + 2.dp else 0.dp
    val avatarRotation = if (rotateWhenPlaying && isPlaying) {
        val spinTransition = rememberInfiniteTransition(label = "reciterAvatarSpin")
        spinTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 10_000, easing = LinearEasing),
            ),
            label = "reciterAvatarRotation",
        ).value
    } else {
        0f
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        if (playbackProgress != null) {
            CircularProgressIndicator(
                progress = { playbackProgress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = progressRingStroke,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                color = MaterialTheme.colorScheme.primary,
                strokeCap = StrokeCap.Round,
            )
        }
        AsyncImage(
            model = imageRequest,
            contentDescription = reciter.nameEnglish,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .padding(avatarInset)
                .rotate(avatarRotation)
                .clip(shape),
        )
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
