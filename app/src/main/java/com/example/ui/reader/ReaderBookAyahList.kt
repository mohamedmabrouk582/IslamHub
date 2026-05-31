package com.example.ui.reader

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Ayah
import com.example.ui.theme.QuranVerseLg

private object MushafColors {
    val pageLight = Color(0xFFFFF9F2)
    val pageDark = Color(0xFF2A2318)
    val borderLight = Color(0xFF8B6F47)
    val borderDark = Color(0xFF6B5638)
    val textLight = Color(0xFF111111)
    val textDark = Color(0xFFE8DFD0)
    val markerRingLight = Color(0xFFB8956A)
    val markerFillLight = Color(0xFFFFF4E8)
    val markerRingDark = Color(0xFF8B7355)
    val markerFillDark = Color(0xFF3D3428)
    val activeHighlightLight = Color(0x66D4B896)
    val activeHighlightDark = Color(0x664A4032)
}

@Composable
fun ReaderBookAyahList(
    surahNumber: Int,
    ayahs: List<Ayah>,
    activeAyahNumber: Int,
    fontSizeProgress: Float,
    showTranslation: Boolean,
    isDarkTheme: Boolean,
    horizontalPadding: androidx.compose.ui.unit.Dp,
    contentMaxWidthDp: Int,
    isCompactWidth: Boolean,
    onAyahClick: (Int) -> Unit,
    onAyahLongClick: (Int) -> Unit,
    onJumpClick: () -> Unit,
) {
    val fontMultiplier = remember(fontSizeProgress) { 0.6f + (fontSizeProgress / 100f) * 0.8f }
    val arabicFontSize = (26 * fontMultiplier).sp
    val arabicLineHeight = (52 * fontMultiplier).sp
    val pageColor = if (isDarkTheme) MushafColors.pageDark else MushafColors.pageLight
    val textColor = if (isDarkTheme) MushafColors.textDark else MushafColors.textLight
    val activeAyah = ayahs.find { it.number == activeAyahNumber }

    val listModifier = Modifier
        .fillMaxSize()
        .then(
            if (!isCompactWidth) {
                Modifier.widthIn(max = contentMaxWidthDp.dp)
            } else {
                Modifier
            },
        )
        .padding(horizontal = horizontalPadding)

    LazyColumn(
        modifier = listModifier,
        contentPadding = PaddingValues(top = 12.dp, bottom = 120.dp),
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(pageColor)
                    .padding(horizontal = 8.dp, vertical = 16.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (surahNumber != 9 && surahNumber != 1) {
                        Text(
                            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                            style = QuranVerseLg,
                            fontSize = (20 * fontMultiplier).sp,
                            lineHeight = (36 * fontMultiplier).sp,
                            color = textColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    } else if (surahNumber != 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    MushafContinuousAyahText(
                        ayahs = ayahs,
                        activeAyahNumber = activeAyahNumber,
                        arabicFontSize = arabicFontSize,
                        arabicLineHeight = arabicLineHeight,
                        textColor = textColor,
                        isDarkTheme = isDarkTheme,
                        onAyahClick = onAyahClick,
                        onAyahLongClick = onAyahLongClick,
                    )
                }
            }
        }

        if (showTranslation && activeAyah != null) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Ayah ${activeAyah.number}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = activeAyah.textEnglish,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onJumpClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = MaterialTheme.shapes.medium,
            ) {
                Icon(Icons.Default.Pin, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Jump or Select Reciter Setup",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun MushafContinuousAyahText(
    ayahs: List<Ayah>,
    activeAyahNumber: Int,
    arabicFontSize: TextUnit,
    arabicLineHeight: TextUnit,
    textColor: Color,
    isDarkTheme: Boolean,
    onAyahClick: (Int) -> Unit,
    onAyahLongClick: (Int) -> Unit,
) {
    val markerSize = 22.sp
    val activeHighlight = if (isDarkTheme) {
        MushafColors.activeHighlightDark
    } else {
        MushafColors.activeHighlightLight
    }

    val annotatedText = remember(ayahs, activeAyahNumber) {
        buildAnnotatedString {
            ayahs.forEach { ayah ->
                val isActive = ayah.number == activeAyahNumber
                pushStringAnnotation(tag = "ayah", annotation = ayah.number.toString())
                withStyle(
                    SpanStyle(
                        background = if (isActive) activeHighlight else Color.Transparent,
                    ),
                ) {
                    append(ayah.textArabic.trim())
                }
                pop()
                append("\u00A0")
                appendInlineContent(id = ayahMarkerId(ayah.number), alternateText = "[${ayah.number}]")
                append("\u00A0")
            }
        }
    }

    val inlineContent = remember(ayahs, activeAyahNumber, isDarkTheme) {
        ayahs.associate { ayah ->
            ayahMarkerId(ayah.number) to InlineTextContent(
                Placeholder(
                    width = markerSize,
                    height = markerSize,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
                ),
            ) {
                MushafOrnateVerseMarker(
                    ayahNumber = ayah.number,
                    isActive = ayah.number == activeAyahNumber,
                    isDarkTheme = isDarkTheme,
                )
            }
        }
    }

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    Text(
        text = annotatedText,
        inlineContent = inlineContent,
        style = QuranVerseLg.copy(
            fontSize = arabicFontSize,
            lineHeight = arabicLineHeight,
            color = textColor,
            textAlign = TextAlign.Center,
            textDirection = TextDirection.Rtl,
        ),
        onTextLayout = { textLayoutResult = it },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .pointerInput(annotatedText) {
                detectTapGestures(
                    onTap = { position ->
                        textLayoutResult?.let { layout ->
                            val offset = layout.getOffsetForPosition(position)
                            layout.layoutInput.text
                                .getStringAnnotations("ayah", offset, offset)
                                .firstOrNull()
                                ?.let { annotation ->
                                    annotation.item.toIntOrNull()?.let(onAyahClick)
                                }
                        }
                    },
                    onLongPress = { position ->
                        textLayoutResult?.let { layout ->
                            val offset = layout.getOffsetForPosition(position)
                            layout.layoutInput.text
                                .getStringAnnotations("ayah", offset, offset)
                                .firstOrNull()
                                ?.let { annotation ->
                                    annotation.item.toIntOrNull()?.let(onAyahLongClick)
                                }
                        }
                    },
                )
            },
    )
}

@Composable
private fun MushafOrnateVerseMarker(
    ayahNumber: Int,
    isActive: Boolean,
    isDarkTheme: Boolean,
) {
    val ringColor = if (isActive) {
        MaterialTheme.colorScheme.secondary
    } else if (isDarkTheme) {
        MushafColors.markerRingDark
    } else {
        MushafColors.markerRingLight
    }
    val fillColor = if (isActive) {
        MaterialTheme.colorScheme.secondaryContainer
    } else if (isDarkTheme) {
        MushafColors.markerFillDark
    } else {
        MushafColors.markerFillLight
    }
    val numberColor = if (isActive) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else if (isDarkTheme) {
        MushafColors.textDark
    } else {
        MushafColors.borderLight
    }

    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            val petalPath = Path().apply {
                val petals = 12
                val innerRadius = radius * 0.82f
                val outerRadius = radius * 0.98f
                for (index in 0 until petals * 2) {
                    val angle = Math.PI * index / petals - Math.PI / 2
                    val r = if (index % 2 == 0) outerRadius else innerRadius
                    val x = center.x + (r * kotlin.math.cos(angle)).toFloat()
                    val y = center.y + (r * kotlin.math.sin(angle)).toFloat()
                    if (index == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            drawPath(petalPath, color = fillColor)
            drawPath(petalPath, color = ringColor, style = Stroke(width = 1.2f))
            drawCircle(color = ringColor, radius = radius * 0.55f, center = center, style = Stroke(width = 0.8f))
        }
        Text(
            text = toArabicIndicNumerals(ayahNumber),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = numberColor,
        )
    }
}

private fun ayahMarkerId(ayahNumber: Int): String = "ayah_marker_$ayahNumber"

private fun toArabicIndicNumerals(number: Int): String {
    return number.toString().map { digit ->
        when (digit) {
            '0' -> '٠'
            '1' -> '١'
            '2' -> '٢'
            '3' -> '٣'
            '4' -> '٤'
            '5' -> '٥'
            '6' -> '٦'
            '7' -> '٧'
            '8' -> '٨'
            '9' -> '٩'
            else -> digit
        }
    }.joinToString("")
}

@Composable
fun ReaderBismillahHeader(
    bismillahFontSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = modifier.fillMaxWidth(),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
        ) {
            Text(
                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                style = QuranVerseLg,
                fontSize = bismillahFontSize,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
        }
    }
}
