package com.example.zelo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.zelo.ui.home.InicioScreen
import com.example.zelo.ui.perfil.PerfilScreen
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

                var telaAtual by remember { mutableStateOf("inicio") }

                Scaffold(
                    bottomBar = {
                        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
                                label = { Text("Início") },
                                selected = telaAtual == "inicio",
                                onClick = { telaAtual = "inicio" },
                                colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.secondaryContainer)
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.DateRange, contentDescription = "Agenda") },
                                label = { Text("Agenda") },
                                selected = telaAtual == "agenda",
                                onClick = {
                                    val intent = Intent(this@MainActivity, AgendaActivity::class.java)
                                    startActivity(intent)
                                }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Notifications, contentDescription = "Lembretes") },
                                label = { Text("Lembretes") },
                                selected = telaAtual == "lembretes",
                                onClick = { telaAtual = "lembretes" }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                                label = { Text("Perfil") },
                                selected = telaAtual == "perfil",
                                onClick = { telaAtual = "perfil" },
                                colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.secondaryContainer)
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {

                        when (telaAtual) {
                            "inicio" -> InicioScreen(
                                uiState = uiState,
                                onAbrirAgenda = { startActivity(Intent(this@MainActivity, AgendaActivity::class.java)) }
                            )

                            "perfil" -> PerfilScreen()

                            "lembretes" -> {
                                // TODO (André,Isa ou Henrique): Quando a Tela de Lembretes estiver pronta,
                                // apague este Text e chame a sua função LembretesScreen() aqui.
                                Text(text = "Tela de Lembretes em construção...")
                            }
                        }
                    }
                }
            }
        }
    }
}