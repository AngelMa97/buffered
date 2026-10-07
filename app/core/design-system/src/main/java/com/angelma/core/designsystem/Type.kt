package com.angelma.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

val Inter = FontFamily(
    Font(resId = R.font.inter_regular, weight = FontWeight.Normal),
    Font(resId = R.font.inter_medium, weight = FontWeight.Medium),
    Font(resId = R.font.inter_bold, weight = FontWeight.Bold),
)

private val base = Typography()

// Same sizes and weights as Material 3; only the font family changes, across all 15 styles.
val Typography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = Inter),
    displayMedium = base.displayMedium.copy(fontFamily = Inter),
    displaySmall = base.displaySmall.copy(fontFamily = Inter),
    headlineLarge = base.headlineLarge.copy(fontFamily = Inter),
    headlineMedium = base.headlineMedium.copy(fontFamily = Inter),
    headlineSmall = base.headlineSmall.copy(fontFamily = Inter),
    titleLarge = base.titleLarge.copy(fontFamily = Inter),
    titleMedium = base.titleMedium.copy(fontFamily = Inter),
    titleSmall = base.titleSmall.copy(fontFamily = Inter),
    bodyLarge = base.bodyLarge.copy(fontFamily = Inter),
    bodyMedium = base.bodyMedium.copy(fontFamily = Inter),
    bodySmall = base.bodySmall.copy(fontFamily = Inter),
    labelLarge = base.labelLarge.copy(fontFamily = Inter),
    labelMedium = base.labelMedium.copy(fontFamily = Inter),
    labelSmall = base.labelSmall.copy(fontFamily = Inter),
)
