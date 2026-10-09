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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.zelo.data.repository.AppColorTheme

object ZeloTheme {

    val spacing: ZeloSpacing
        @Composable
        get() = LocalZeloSpacing.current

    val headerGradient: Brush
        @Composable
        get() = Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.primaryContainer
            )
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
    error = Color(0xFFB3261E)
)

private val ZeloDarkColors = darkColorScheme(
    primary = Color(0xFFE0876A),
    onPrimary = Color(0xFF2A140C),
    background = Color(0xFF1C1712),
    surface = Color(0xFF241E18),
    onBackground = Color(0xFFF0E8E0),
    onSurfaceVariant = Color(0xFFC4B8AE)
)

private val GreenColorScheme = lightColorScheme(
    primary = Color(0xFF388E6C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD5EFE1),
    onPrimaryContainer = Color(0xFF123F30),
    secondary = Color(0xFF5E8C75),
    secondaryContainer = Color(0xFFE0F1E7),
    onSecondaryContainer = Color(0xFF244D37),
    background = Color(0xFFF7FBF8),
    onBackground = Color(0xFF1A2921),
    surface = Color.White,
    onSurface = Color(0xFF1A2921),
    surfaceVariant = Color(0xFFE6F0EA),
    onSurfaceVariant = Color(0xFF53665B),
    outline = Color(0xFFA6B9AD),
    outlineVariant = Color(0xFFD6E4D9),
    error = Color(0xFFB3261E)
)

private val BlueColorScheme = lightColorScheme(
    primary = Color(0xFF3978B8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8E9FA),
    onPrimaryContainer = Color(0xFF163E68),
    secondary = Color(0xFF648EBA),
    secondaryContainer = Color(0xFFE1EEFB),
    onSecondaryContainer = Color(0xFF244D79),
    background = Color(0xFFF7FAFE),
    onBackground = Color(0xFF1A2735),
    surface = Color.White,
    onSurface = Color(0xFF1A2735),
    surfaceVariant = Color(0xFFE7EFF7),
    onSurfaceVariant = Color(0xFF56697D),
    outline = Color(0xFFA8B9CA),
    outlineVariant = Color(0xFFD8E3EF),
    error = Color(0xFFB3261E)
)

@Composable
fun ZeloTheme(
    temaCor: AppColorTheme = AppColorTheme.ORIGINAL,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when (temaCor) {
        AppColorTheme.ORIGINAL -> {
            if (darkTheme) ZeloDarkColors else ZeloLightColors
        }

        AppColorTheme.VERDE -> GreenColorScheme

        AppColorTheme.AZUL -> BlueColorScheme
    }

    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            window.statusBarColor = colorScheme.primary.toArgb()

            WindowCompat
                .getInsetsController(window, view)
                .isAppearanceLightStatusBars = false
        }
    }

    CompositionLocalProvider(
        LocalZeloSpacing provides ZeloSpacing()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ZeloTypography,
            shapes = ZeloShapes,
            content = content
        )
    }
}