package com.example.zelo.ui.demo

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** Local correction for surface-colored demo headers; leaves the app's other screens unchanged. */
@Composable
fun DemoSystemBars() {
    val view = LocalView.current
    val lightSurface = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    if (!view.isInEditMode) SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        val controller = WindowCompat.getInsetsController(window, view)
        // ZeloTheme sets white status icons globally; these surface headers need contrast.
        controller.isAppearanceLightStatusBars = lightSurface
        controller.isAppearanceLightNavigationBars = lightSurface
    }
}
