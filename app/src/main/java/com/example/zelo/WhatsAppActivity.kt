package com.example.zelo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.zelo.ui.lembretes.WhatsAppScreen
import com.example.zelo.ui.theme.ZeloTheme
import com.example.zelo.viewmodel.AppThemeViewModel
import com.example.zelo.viewmodel.WhatsAppViewModel

class WhatsAppActivity : ComponentActivity() {
    private val model: WhatsAppViewModel by viewModels()
    private val theme: AppThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val selected by theme.tema.collectAsState()
            ZeloTheme(temaCor = selected) {
                WhatsAppScreen(model, onBack = { finish() }, onOpenDemo = {
                    startActivity(android.content.Intent(this, WhatsAppDemoActivity::class.java)
                        .putExtra("DEMO_THEME", selected.name))
                })
            }
        }
    }
}
