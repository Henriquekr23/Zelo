package com.example.zelo.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

import androidx.compose.ui.graphics.Brush

// --- Add this object to access the theme easily ---
object ZeloTheme {
    val spacing: ZeloSpacing
        @Composable
        get() = LocalZeloSpacing.current

    val headerGradient: Brush
        @Composable
        get() = Brush.linearGradient(
            colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
        )
}

private val ZeloLightColors = lightColorScheme(
    primary = ClayPrimary,
    onPrimary = OnPrimary,
    primaryContainer = ClayPrimaryVariant,
    onPrimaryContainer = OnPrimary,
    secondaryContainer = SageContainer,
    onSecondaryContainer = OnSageContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnBackground,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = OutlineVariant,
    error = Color(0xFFB3261E),
)

private val ZeloDarkColors = darkColorScheme(
    primary = Color(0xFFE0876A),
    onPrimary = Color(0xFF2A140C),
    background = Color(0xFF1C1712),
    surface = Color(0xFF241E18),
    onBackground = Color(0xFFF0E8E0),
    onSurfaceVariant = Color(0xFFC4B8AE),
)

@Composable
fun ZeloTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) ZeloDarkColors else ZeloLightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    CompositionLocalProvider(LocalZeloSpacing provides ZeloSpacing()) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ZeloTypography,
            shapes = ZeloShapes,
            content = content
        )
    }
}
