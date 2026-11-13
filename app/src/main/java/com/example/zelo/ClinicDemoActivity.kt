package com.example.zelo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.zelo.data.repository.AppColorTheme
import com.example.zelo.ui.clinic.ClinicDemoScreen
import com.example.zelo.ui.demo.DemoSystemBars
import com.example.zelo.ui.theme.ZeloTheme
import com.example.zelo.viewmodel.ClinicDemoViewModel

class ClinicDemoActivity : ComponentActivity() {
    private val model: ClinicDemoViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val selected = AppColorTheme.entries.firstOrNull { it.name == intent.getStringExtra("DEMO_THEME") }
            ?: AppColorTheme.ORIGINAL
        setContent {
            ZeloTheme(temaCor = selected) {
                DemoSystemBars()
                ClinicDemoScreen(model, onBack = { finish() })
            }
        }
    }
}
