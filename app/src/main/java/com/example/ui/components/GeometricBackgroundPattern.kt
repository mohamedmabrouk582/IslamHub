package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun GeometricBackgroundPattern() {
    val color = MaterialTheme.colorScheme.primary.copy(alpha = 0.03f)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val step = 80.dp.toPx()
                for (x in 0..size.width.toInt() step step.toInt()) {
                    for (y in 0..size.height.toInt() step step.toInt()) {
                        drawCircle(
                            color = color,
                            radius = 2.dp.toPx(),
                            center = Offset(x.toFloat(), y.toFloat()),
                        )
                    }
                }
            },
    )
}

@Composable
fun RubElHizbIcon(number: Int, modifier: Modifier = Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(48.dp)
            .drawBehind {
                val sizePx = size.minDimension
                val half = sizePx / 2f
                val third = sizePx / 3f

                val path1 = Path().apply {
                    moveTo(half, 0f)
                    lineTo(sizePx, third)
                    lineTo(sizePx, sizePx - third)
                    lineTo(half, sizePx)
                    lineTo(0f, sizePx - third)
                    lineTo(0f, third)
                    close()
                }

                val path2 = Path().apply {
                    moveTo(third, 0f)
                    lineTo(sizePx - third, 0f)
                    lineTo(sizePx, half)
                    lineTo(sizePx - third, sizePx)
                    lineTo(third, sizePx)
                    lineTo(0f, half)
                    close()
                }

                drawPath(path1, color = primaryColor.copy(alpha = 0.08f))
                drawPath(path1, color = primaryColor, style = Stroke(width = 1.5.dp.toPx()))
                drawPath(path2, color = secondaryColor.copy(alpha = 0.08f))
                drawPath(path2, color = secondaryColor, style = Stroke(width = 1.dp.toPx()))
            },
    ) {
        Text(
            text = number.toString(),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
        )
    }
}
