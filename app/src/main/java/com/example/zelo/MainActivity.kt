package com.example.zelo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.zelo.model.Pet
import com.example.zelo.ui.historico.HistoricoScreen
import com.example.zelo.ui.home.InicioScreen
import com.example.zelo.ui.lembretes.LembretesScreen
import com.example.zelo.ui.perfil.PerfilScreen
import com.example.zelo.ui.splash.ZeloSplashScreen
import com.example.zelo.ui.theme.ZeloTheme
import com.example.zelo.viewmodel.AppThemeViewModel
import com.example.zelo.viewmodel.HistoricoViewModel
import com.example.zelo.viewmodel.InicioViewModel
import com.example.zelo.viewmodel.LembretesViewModel
import com.example.zelo.viewmodel.PetsViewModel
import com.example.zelo.viewmodel.TutorViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val appThemeViewModel: AppThemeViewModel by viewModels()
    private val viewModel: InicioViewModel by viewModels()
    private val historicoViewModel: HistoricoViewModel by viewModels()
    private val lembretesViewModel: LembretesViewModel by viewModels()
    private val petsViewModel: PetsViewModel by viewModels()
    private val tutorViewModel: TutorViewModel by viewModels()

    private val _telaAtual = mutableStateOf("inicio")

    private val _petsPerfil =
        mutableStateOf<List<Pet>>(emptyList())

    private val _petAtivoPerfilId =
        mutableStateOf<Int?>(null)

    private var splashVisivel by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen()

        super.onCreate(savedInstanceState)

        splashVisivel =
            savedInstanceState == null &&
                    intent.action == Intent.ACTION_MAIN &&
                    intent.hasCategory(Intent.CATEGORY_LAUNCHER) &&
                    !intent.hasExtra("TELA")

        intent.getStringExtra("TELA")?.let {
            _telaAtual.value = it
        }

        enableEdgeToEdge()

        petsViewModel.pets.observe(this) { pets ->

            _petsPerfil.value = pets

            val idAtual = _petAtivoPerfilId.value

            if (pets.none { it.id == idAtual }) {

                val primeiroPet = pets.firstOrNull()

                _petAtivoPerfilId.value = primeiroPet?.id

                if (primeiroPet != null) {

                    viewModel.selecionarPet(primeiroPet)

                    historicoViewModel.selecionarPet(
                        primeiroPet.id
                    )
                }
            }
        }

        setContent {

            val temaSelecionado by
            appThemeViewModel.tema.collectAsState()

            LaunchedEffect(splashVisivel) {

                if (splashVisivel) {
                    delay(2500)
                    splashVisivel = false
                }
            }

            if (splashVisivel) {

                ZeloSplashScreen()

            } else {

                ZeloTheme(
                    temaCor = temaSelecionado
                ) {

                    val uiState by viewModel.uiState

                    val historicoUiState by
                    historicoViewModel.uiState

                    val lembretesUiState by
                    lembretesViewModel.uiState

                    val telaAtual by _telaAtual

                    val petsPerfil by _petsPerfil

                    val petAtivoPerfilId by
                    _petAtivoPerfilId

                    val nomeTutor by
                    tutorViewModel.nome.collectAsState()

                    val petAtivoPerfil =
                        petsPerfil.firstOrNull {
                            it.id == petAtivoPerfilId
                        }

                    Scaffold(
                        bottomBar = {

                            NavigationBar(
                                containerColor =
                                    MaterialTheme.colorScheme.surface
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
                                    colors =
                                        NavigationBarItemDefaults.colors(
                                            indicatorColor =
                                                MaterialTheme.colorScheme.secondaryContainer
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

                                        _petAtivoPerfilId.value?.let { petId ->
                                            historicoViewModel.selecionarPet(
                                                petId
                                            )
                                        }

                                        _telaAtual.value = "historico"
                                    },
                                    colors =
                                        NavigationBarItemDefaults.colors(
                                            indicatorColor =
                                                MaterialTheme.colorScheme.secondaryContainer
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
                                    colors =
                                        NavigationBarItemDefaults.colors(
                                            indicatorColor =
                                                MaterialTheme.colorScheme.secondaryContainer
                                        )
                                )
                            }
                        }
                    ) { innerPadding ->

                        Box(
                            modifier = Modifier.padding(
                                bottom =
                                    innerPadding.calculateBottomPadding()
                            )
                        ) {

                            val screenOrder = listOf(
                                "inicio",
                                "lembretes",
                                "agenda",
                                "historico",
                                "perfil"
                            )

                            AnimatedContent(
                                targetState = telaAtual,
                                transitionSpec = {

                                    val initialIndex =
                                        screenOrder.indexOf(initialState)
                                            .takeIf { it != -1 } ?: 0

                                    val targetIndex =
                                        screenOrder.indexOf(targetState)
                                            .takeIf { it != -1 } ?: 0

                                    if (targetIndex > initialIndex) {

                                        (
                                                slideInHorizontally { it } +
                                                        fadeIn()
                                                ).togetherWith(
                                                slideOutHorizontally { -it } +
                                                        fadeOut()
                                            )

                                    } else {

                                        (
                                                slideInHorizontally { -it } +
                                                        fadeIn()
                                                ).togetherWith(
                                                slideOutHorizontally { it } +
                                                        fadeOut()
                                            )
                                    }
                                },
                                label = "MainContentTransition"
                            ) { targetTela ->

                                when (targetTela) {

                                    "inicio" -> InicioScreen(
                                        nomeTutor = nomeTutor,
                                        onEditarPerfil = {
                                            _telaAtual.value = "perfil"
                                        },
                                        uiState = uiState,
                                        onAbrirAgenda = { dataAgendamento ->

                                            val agendaIntent = Intent(
                                                this@MainActivity,
                                                AgendaActivity::class.java
                                            )

                                            if (!dataAgendamento.isNullOrBlank()) {
                                                agendaIntent.putExtra(
                                                    "DATA_AGENDAMENTO",
                                                    dataAgendamento
                                                )
                                            }

                                            startActivity(agendaIntent)
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

                                            if (tela == "historico") {

                                                _petAtivoPerfilId.value?.let { petId ->
                                                    historicoViewModel.selecionarPet(
                                                        petId
                                                    )
                                                }
                                            }

                                            _telaAtual.value = tela
                                        },
                                        onSelecionarPet = { pet ->

                                            viewModel.selecionarPet(pet)

                                            _petAtivoPerfilId.value =
                                                pet.id
                                        }
                                    )

                                    "historico" -> HistoricoScreen(
                                        uiState = historicoUiState,
                                        onSelecionarPet = { petId ->

                                            historicoViewModel.selecionarPet(
                                                petId
                                            )

                                            _petAtivoPerfilId.value =
                                                petId

                                            petsPerfil.firstOrNull {
                                                it.id == petId
                                            }?.let { pet ->

                                                viewModel.selecionarPet(pet)
                                            }
                                        }
                                    )

                                    "lembretes" -> LembretesScreen(
                                        uiState = lembretesUiState
                                    )

                                    "perfil" -> PerfilScreen(
                                        temaSelecionado = temaSelecionado,
                                        onSelecionarTema = { novoTema ->

                                            appThemeViewModel.selecionarTema(
                                                novoTema
                                            )
                                        },
                                        nomeTutor = nomeTutor,
                                        onSalvarNomeTutor = { novoNome ->

                                            tutorViewModel.atualizarNome(
                                                novoNome
                                            )
                                        },
                                        todosPets = petsPerfil,
                                        petAtivo = petAtivoPerfil,
                                        onSelecionarPet = { pet ->

                                            _petAtivoPerfilId.value = pet.id

                                            viewModel.selecionarPet(pet)

                                            historicoViewModel.selecionarPet(
                                                pet.id
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
                                        onAbrirHistorico = {

                                            petAtivoPerfil?.let { pet ->

                                                historicoViewModel.selecionarPet(
                                                    pet.id
                                                )
                                            }

                                            _telaAtual.value = "historico"
                                        }
                                    )
                                }
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

        splashVisivel = false

        intent.getStringExtra("TELA")?.let {
            _telaAtual.value = it
        }
    }
}