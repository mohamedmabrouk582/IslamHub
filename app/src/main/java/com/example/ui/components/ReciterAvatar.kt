package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
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
) {
    Box(modifier = modifier.size(size)) {
        Image(
            painter = painterResource(reciter.avatarResId),
            contentDescription = reciter.nameEnglish,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(shape),
        )
        if (showRing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(3.dp, MaterialTheme.colorScheme.surface, shape),
            )
        }
    }
}
