package com.example.zelo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import com.example.zelo.ui.home.InicioScreen
import com.example.zelo.ui.theme.ZeloTheme
import com.example.zelo.viewmodel.InicioViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: InicioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            ZeloTheme {
                val uiState by viewModel.uiState

                InicioScreen(
                    uiState = uiState,
                    onAbrirAgenda = {
                        val intent = Intent(
                            this,
                            AgendaActivity::class.java
                        )

                        startActivity(intent)
                    }
                )
            }
        }
    }
}