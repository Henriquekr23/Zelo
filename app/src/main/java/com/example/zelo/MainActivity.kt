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
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.zelo.ui.home.InicioScreen
import com.example.zelo.ui.historico.HistoricoScreen
import com.example.zelo.ui.perfil.PerfilScreen
import com.example.zelo.ui.theme.ZeloTheme
import com.example.zelo.viewmodel.InicioViewModel
import com.example.zelo.viewmodel.HistoricoViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: InicioViewModel by viewModels()
    private val historicoViewModel: HistoricoViewModel by viewModels()
    private val _telaAtual = mutableStateOf("inicio")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent.getStringExtra("TELA")?.let {
            _telaAtual.value = it
        }

        enableEdgeToEdge()
        setContent {
            ZeloTheme {
                val uiState by viewModel.uiState
                val historicoUiState by historicoViewModel.uiState
                val telaAtual by _telaAtual

                Scaffold(
                    bottomBar = {
                        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
                                label = { Text("Início") },
                                selected = telaAtual == "inicio",
                                onClick = { _telaAtual.value = "inicio" },
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
                                icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Histórico") },
                                label = { Text("Histórico") },
                                selected = telaAtual == "historico",
                                onClick = { _telaAtual.value = "historico" },
                                colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.secondaryContainer)
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                                label = { Text("Perfil") },
                                selected = telaAtual == "perfil",
                                onClick = { _telaAtual.value = "perfil" },
                                colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.secondaryContainer)
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {

                        when (telaAtual) {
                            "inicio" -> InicioScreen(
                                uiState = uiState,
                                onAbrirAgenda = { startActivity(Intent(this@MainActivity, AgendaActivity::class.java)) },
                                onAbrirMeusPets = { startActivity(Intent(this@MainActivity, MeusPetsActivity::class.java)) },
                                onNavegarParaTela = { tela -> _telaAtual.value = tela }
                            )

                            "perfil" -> PerfilScreen(
                                onNavegarParaHistorico = { _telaAtual.value = "historico" },
                                onAbrirMeusPets = { startActivity(Intent(this@MainActivity, MeusPetsActivity::class.java)) }
                            )

                            "historico" -> HistoricoScreen(uiState = historicoUiState)
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra("TELA")?.let {
            _telaAtual.value = it
        }
    }
}
