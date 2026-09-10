package com.example.zelo.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.zelo.R

// Nota: Coloque os arquivos .ttf em res/font/ (Source Serif 4 e Plus Jakarta Sans)
val SourceSerif4 = FontFamily(
    // Font(R.font.source_serif_4_medium, FontWeight.Medium),
    // Font(R.font.source_serif_4_semibold, FontWeight.SemiBold),
    FontFamily.Serif.fontFamily.first() // Fallback enquanto não há arquivos de fonte
)

val PlusJakartaSans = FontFamily(
    // Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    // Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    // Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    // Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
    FontFamily.SansSerif.fontFamily.first() // Fallback enquanto não há arquivos de fonte
)

val ZeloTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = SourceSerif4,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = SourceSerif4,
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = SourceSerif4,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.5.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
    ),
)
