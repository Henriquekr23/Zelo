package com.example.zelo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.example.zelo.ui.historico.HistoricoScreen
import com.example.zelo.ui.home.InicioScreen
import com.example.zelo.ui.lembretes.LembretesScreen
import com.example.zelo.ui.perfil.PerfilScreen
import com.example.zelo.ui.theme.ZeloTheme
import com.example.zelo.viewmodel.HistoricoViewModel
import com.example.zelo.viewmodel.InicioViewModel
import com.example.zelo.viewmodel.LembretesViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: InicioViewModel by viewModels()
    private val historicoViewModel: HistoricoViewModel by viewModels()
    private val lembretesViewModel: LembretesViewModel by viewModels()

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
                val lembretesUiState by lembretesViewModel.uiState
                val telaAtual by _telaAtual

                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        Icons.Default.Home,
                                        contentDescription = "Início"
                                    )
                                },
                                label = {
                                    Text("Início")
                                },
                                selected = telaAtual == "inicio",
                                onClick = {
                                    _telaAtual.value = "inicio"
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            )

                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        Icons.Default.DateRange,
                                        contentDescription = "Agenda"
                                    )
                                },
                                label = {
                                    Text("Agenda")
                                },
                                selected = false,
                                onClick = {
                                    startActivity(
                                        Intent(
                                            this@MainActivity,
                                            AgendaActivity::class.java
                                        )
                                    )
                                }
                            )

                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        Icons.AutoMirrored.Filled.List,
                                        contentDescription = "Histórico"
                                    )
                                },
                                label = {
                                    Text("Histórico")
                                },
                                selected = telaAtual == "historico",
                                onClick = {
                                    _telaAtual.value = "historico"
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            )

                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = "Perfil"
                                    )
                                },
                                label = {
                                    Text("Perfil")
                                },
                                selected = telaAtual == "perfil",
                                onClick = {
                                    _telaAtual.value = "perfil"
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            )
                        }
                    }
                ) { innerPadding ->

                    Box(
                        modifier = Modifier.padding(
                            bottom = innerPadding.calculateBottomPadding()
                        )
                    ) {
                        val screenOrder = listOf("inicio", "lembretes", "agenda", "historico", "perfil")

                        AnimatedContent(
                            targetState = telaAtual,
                            transitionSpec = {
                                val initialIndex = screenOrder.indexOf(initialState).takeIf { it != -1 } ?: 0
                                val targetIndex = screenOrder.indexOf(targetState).takeIf { it != -1 } ?: 0

                                if (targetIndex > initialIndex) {
                                    (slideInHorizontally { it } + fadeIn())
                                        .togetherWith(slideOutHorizontally { -it } + fadeOut())
                                } else {
                                    (slideInHorizontally { -it } + fadeIn())
                                        .togetherWith(slideOutHorizontally { it } + fadeOut())
                                }
                            },
                            label = "MainContentTransition"
                        ) { targetTela ->
                            when (targetTela) {

                                "inicio" -> InicioScreen(
                                    uiState = uiState,
                                    onAbrirAgenda = {
                                        startActivity(
                                            Intent(
                                                this@MainActivity,
                                                AgendaActivity::class.java
                                            )
                                        )
                                    },
                                    onAbrirMeusPets = {
                                        startActivity(
                                            Intent(
                                                this@MainActivity,
                                                MeusPetsActivity::class.java
                                            )
                                        )
                                    },
                                    onNavegarParaTela = { tela ->
                                        _telaAtual.value = tela
                                    },
                                    onSelecionarPet = { pet ->
                                        viewModel.selecionarPet(pet)
                                    }
                                )

                                "historico" -> HistoricoScreen(
                                    uiState = historicoUiState
                                )

                                "lembretes" -> LembretesScreen(
                                    uiState = lembretesUiState
                                )

                                "perfil" -> PerfilScreen(
                                    todosPets = uiState.todosPets,
                                    petAtivo = uiState.petAtivo,
                                    onSelecionarPet = { pet ->
                                        viewModel.selecionarPet(pet)
                                    },
                                    onAbrirMeusPets = {
                                        startActivity(
                                            Intent(
                                                this@MainActivity,
                                                MeusPetsActivity::class.java
                                            )
                                        )
                                    }
                                )
                            }
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