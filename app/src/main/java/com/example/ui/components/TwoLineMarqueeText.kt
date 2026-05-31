package com.example.ui.components

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

@Composable
fun TwoLineMarqueeText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle,
    fontSize: TextUnit = style.fontSize,
    lineHeight: TextUnit = style.lineHeight,
    textAlign: TextAlign = TextAlign.Center,
    maxLines: Int = 3,
) {
    Text(
        text = text,
        style = style.copy(
            fontSize = fontSize,
            lineHeight = lineHeight,
            textAlign = textAlign,
        ),
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .fillMaxWidth()
            .basicMarquee(
                iterations = Int.MAX_VALUE,
                initialDelayMillis = 1_000,
                repeatDelayMillis = 1_500,
            ),
    )
}
