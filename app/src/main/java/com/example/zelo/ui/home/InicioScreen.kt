
package com.example.zelo.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.zelo.model.Agendamento
import com.example.zelo.model.Pet
import com.example.zelo.ui.theme.WarningContainer
import com.example.zelo.viewmodel.InicioUiState

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun InicioScreen(
    uiState: InicioUiState,
    nomeTutor: String,
    onEditarPerfil: () -> Unit,
    onAbrirAgenda: () -> Unit,
    onAbrirMeusPets: () -> Unit,
    onNavegarParaTela: (String) -> Unit,
    onSelecionarPet: (Pet) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        CabecalhoInicio(
            uiState = uiState,
            nomeTutor = nomeTutor,
            onEditarPerfil = onEditarPerfil,
            onAbrirMeusPets = onAbrirMeusPets,
            onSelecionarPet = onSelecionarPet
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {

            // Próximo lembrete real
            LembreteCard(
                procedimento = uiState.proximoLembrete,
                onClick = onAbrirAgenda
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Ações rápidas
            Text(
                text = "Ações rápidas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            GridAcoesRapidas(
                onAbrirAgenda = onAbrirAgenda,
                onAbrirMeusPets = onAbrirMeusPets,
                onNavegarParaTela = onNavegarParaTela
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Próximos cuidados
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "Próximos cuidados",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                TextButton(
                    onClick = onAbrirAgenda
                ) {
                    Text("Ver tudo")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.proximosCuidados.isEmpty()) {

                Card(
                    modifier = Modifier.fillMaxWidth(),

                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.surface
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "🐾",
                            fontSize = 32.sp
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text = "Nenhum cuidado agendado",
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "Os próximos cuidados do seu pet aparecerão aqui.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

            } else {

                uiState.proximosCuidados.forEach { procedimento ->

                    CuidadoCard(
                        procedimento = procedimento,
                        onClick = onAbrirAgenda
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onAbrirAgenda,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agendar consulta")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// =====================================================
// CABEÇALHO DINÂMICO
// =====================================================

@Composable
private fun CabecalhoInicio(
    uiState: InicioUiState,
    nomeTutor: String,
    onEditarPerfil: () -> Unit,
    onAbrirMeusPets: () -> Unit,
    onSelecionarPet: (Pet) -> Unit
) {

    var menuExpandido by remember {
        mutableStateOf(false)
    }

    val petAtivo = uiState.petAtivo

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .statusBarsPadding()
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 20.dp,
                bottom = 24.dp
            )
    ) {

        Text(
            text = obterSaudacao(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = nomeTutor,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.weight(1f)
            )

            TextButton(
                onClick = onEditarPerfil
            ) {
                Text(
                    text = "Editar",
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // Nenhum animal cadastrado
        if (petAtivo == null) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onAbrirMeusPets()
                    },

                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.surface
                )
            ) {

                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "🐾",
                        fontSize = 36.sp
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "Nenhum pet cadastrado",
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Toque para adicionar seu primeiro animal.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

        } else {

            // Animal selecionado
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            menuExpandido = true
                        },

                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.surface
                    )
                ) {

                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    MaterialTheme.colorScheme.secondaryContainer
                                ),

                            contentAlignment = Alignment.Center
                        ) {

                            Text(
                                text = petAtivo.emoji.ifBlank { "🐾" },
                                fontSize = 26.sp
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp)
                        ) {

                            Text(
                                text = petAtivo.nome,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "${petAtivo.especie} • ${petAtivo.raca}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector =
                                Icons.Default.KeyboardArrowDown,

                            contentDescription = "Selecionar pet",

                            tint =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Menu com pets reais do Room
                DropdownMenu(
                    expanded = menuExpandido,

                    onDismissRequest = {
                        menuExpandido = false
                    },

                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {

                    uiState.todosPets.forEach { pet ->

                        DropdownMenuItem(
                            text = {

                                Row(
                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {

                                    Text(
                                        text = pet.emoji.ifBlank { "🐾" },
                                        fontSize = 22.sp,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )

                                    Text(
                                        text = pet.nome,

                                        fontWeight =
                                            if (pet.id == petAtivo.id)
                                                FontWeight.Bold
                                            else
                                                FontWeight.Normal
                                    )
                                }
                            },

                            onClick = {

                                onSelecionarPet(pet)

                                menuExpandido = false
                            }
                        )
                    }
                }
            }
        }
    }
}

// =====================================================
// LEMBRETE DINÂMICO
// =====================================================

@Composable
private fun LembreteCard(
    procedimento: Agendamento?,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = procedimento != null,
                onClick = onClick
            ),

        colors = CardDefaults.cardColors(
            containerColor = WarningContainer
        )
    ) {

        if (procedimento == null) {

            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "✅",
                    fontSize = 24.sp
                )

                Column(
                    modifier = Modifier.padding(start = 12.dp)
                ) {

                    Text(
                        text = "Tudo em dia!",
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Nenhum lembrete próximo para este pet.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

        } else {

            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFB35C)),

                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "📅",
                        fontSize = 22.sp
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {

                    Text(
                        text = procedimento.descricao,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = textoPrazo(procedimento.data),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "${procedimento.data} às ${procedimento.horario}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// =====================================================
// AÇÕES RÁPIDAS
// =====================================================

@Composable
private fun GridAcoesRapidas(
    onAbrirAgenda: () -> Unit,
    onAbrirMeusPets: () -> Unit,
    onNavegarParaTela: (String) -> Unit
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {

            ItemAcaoRapida(
                titulo = "Agenda",
                descricao = "Ver consultas",
                icone = "📅",
                modifier = Modifier.weight(1f),
                onClick = onAbrirAgenda
            )

            ItemAcaoRapida(
                titulo = "Histórico",
                descricao = "Cuidados passados",
                icone = "📄",
                modifier = Modifier.weight(1f),
                onClick = {
                    onNavegarParaTela("historico")
                }
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {

            ItemAcaoRapida(
                titulo = "Meus Pets",
                descricao = "Gerenciar perfis",
                icone = "🐶",
                modifier = Modifier.weight(1f),
                onClick = onAbrirMeusPets
            )

            ItemAcaoRapida(
                titulo = "Perfil",
                descricao = "Configurações",
                icone = "⚙️",
                modifier = Modifier.weight(1f),
                onClick = {
                    onNavegarParaTela("perfil")
                }
            )
        }
    }
}

@Composable
private fun ItemAcaoRapida(
    titulo: String,
    descricao: String,
    icone: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Card(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),

        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surface
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            Text(
                text = icone,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = titulo,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = descricao,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// =====================================================
// PRÓXIMOS CUIDADOS
// =====================================================

@Composable
private fun CuidadoCard(
    procedimento: Agendamento,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),

        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surface
        )
    ) {

        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.primary
                    )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {

                Text(
                    text = procedimento.descricao,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "${procedimento.data} às ${procedimento.horario}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = textoPrazo(procedimento.data),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "AGENDADO",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// =====================================================
// FUNÇÕES AUXILIARES
// =====================================================

private fun obterSaudacao(): String {

    val hora = Calendar.getInstance()
        .get(Calendar.HOUR_OF_DAY)

    return when (hora) {

        in 5..11 -> "Bom dia,"

        in 12..17 -> "Boa tarde,"

        else -> "Boa noite,"
    }
}

private fun textoPrazo(data: String): String {

    val formato = DateTimeFormatter.ofPattern(
        "dd/MM/uuuu",
        Locale("pt", "BR")
    )

    val dataAgendada = try {
        LocalDate.parse(data, formato)
    } catch (e: Exception) {
        return data
    }

    val hoje = LocalDate.now()

    val dias = ChronoUnit.DAYS.between(
        hoje,
        dataAgendada
    )

    return when {

        dias == 0L -> "Hoje"

        dias == 1L -> "Amanhã"

        dias > 1L -> "Em $dias dias"

        dias == -1L -> "Ontem"

        else -> "Há ${-dias} dias"
    }
}
