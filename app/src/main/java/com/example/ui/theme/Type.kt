package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.sp

// We can define custom text styles as constants for our Arabic and Latin fonts
val DisplayArabic = TextStyle(
    fontFamily = FontFamily.Serif,
    fontWeight = FontWeight.Normal,
    fontSize = 45.sp,
    lineHeight = 64.sp,
    textDirection = TextDirection.Rtl
)

val HeadlineLgMobile = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    lineHeight = 36.sp
)

val TitleMd = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.15.sp
)

val QuranVerseLg = TextStyle(
    fontFamily = FontFamily.Serif,
    fontWeight = FontWeight.Normal,
    fontSize = 34.sp,
    lineHeight = 68.sp,
    textDirection = TextDirection.Rtl
)

val QuranVerseSm = TextStyle(
    fontFamily = FontFamily.Serif,
    fontWeight = FontWeight.Normal,
    fontSize = 24.sp,
    lineHeight = 52.sp,
    textDirection = TextDirection.Rtl
)

val BodyLg = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp
)

val BodyMdCustom = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp
)

val LabelSmCustom = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp
)

val Typography = Typography(
    displayLarge = DisplayArabic,
    headlineMedium = HeadlineLgMobile,
    titleMedium = TitleMd,
    bodyLarge = BodyLg,
    bodyMedium = BodyMdCustom,
    labelSmall = LabelSmCustom
)
