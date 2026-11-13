package com.example.zelo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.zelo.data.repository.AppColorTheme
import com.example.zelo.ui.lembretes.WhatsAppDemoScreen
import com.example.zelo.ui.theme.ZeloTheme
import com.example.zelo.viewmodel.WhatsAppDemoViewModel

/** The demo reads only a theme identifier from its Intent; no application data is loaded. */
class WhatsAppDemoActivity : ComponentActivity() {
    private val model: WhatsAppDemoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val selectedTheme = AppColorTheme.entries.firstOrNull {
            it.name == intent.getStringExtra("DEMO_THEME")
        } ?: AppColorTheme.ORIGINAL
        setContent {
            ZeloTheme(temaCor = selectedTheme) {
                WhatsAppDemoScreen(model, onBack = { finish() })
            }
        }
    }
}
