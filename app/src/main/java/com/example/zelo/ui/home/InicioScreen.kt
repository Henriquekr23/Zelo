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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.zelo.model.Procedimento
import com.example.zelo.model.TipoProcedimento
import com.example.zelo.ui.theme.WarningContainer
import com.example.zelo.viewmodel.InicioUiState

@Composable
fun InicioScreen(
    uiState: InicioUiState,
    onAbrirAgenda: () -> Unit,
    onNavegarParaTela: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CabecalhoInicio(uiState)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
                LembreteCard(uiState.proximoLembrete)

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Ações rápidas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                GridAcoesRapidas(
                    onAbrirAgenda = onAbrirAgenda,
                    onNavegarParaTela = onNavegarParaTela
                )

                Spacer(modifier = Modifier.height(24.dp))

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
                        Text(text = "Ver tudo")
                    }
                }

                uiState.proximosCuidados.forEach { procedimento ->
                    CuidadoCard(procedimento)
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onAbrirAgenda,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Agendar consulta")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
}

@Composable
private fun GridAcoesRapidas(
    onAbrirAgenda: () -> Unit,
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
                onClick = { onNavegarParaTela("historico") }
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
                onClick = { onNavegarParaTela("perfil") }
            )
            ItemAcaoRapida(
                titulo = "Perfil",
                descricao = "Configurações",
                icone = "⚙️",
                modifier = Modifier.weight(1f),
                onClick = { onNavegarParaTela("perfil") }
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = icone, style = MaterialTheme.typography.titleLarge)
            Text(text = titulo, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            Text(text = descricao, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CabecalhoInicio(
    uiState: InicioUiState
) {
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
            text = "Boa tarde,",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )

        Text(
            text = uiState.tutor.nome,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🐾",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    Text(
                        text = uiState.petAtivo.nome,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${uiState.petAtivo.especie} • " +
                                uiState.petAtivo.raca,
                        style = MaterialTheme.typography.bodyMedium,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "⌄",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}

@Composable
private fun LembreteCard(
    procedimento: Procedimento?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = WarningContainer
        )
    ) {
        if (procedimento == null) {
            Text(
                text = "Nenhum lembrete próximo.",
                modifier = Modifier.padding(20.dp)
            )
        } else {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFB35C)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "▣")
                }

                Column(
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Text(
                        text = "${procedimento.descricao} vence em 5 dias",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Enviaremos um lembrete em breve.",
                        style = MaterialTheme.typography.bodySmall,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CuidadoCard(
    procedimento: Procedimento
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(corDoProcedimento(procedimento.tipo))
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

                Text(
                    text = textoSecundario(procedimento),
                    style = MaterialTheme.typography.bodySmall,
                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (procedimento.tipo == TipoProcedimento.VACINA) {
                Text(
                    text = "EM BREVE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun textoSecundario(
    procedimento: Procedimento
): String {
    return when (procedimento.tipo) {
        TipoProcedimento.VACINA ->
            "Vence em 5 dias • ${procedimento.data}"

        TipoProcedimento.VERMIFUGO ->
            "Vence em 3 semanas • ${procedimento.data}"

        TipoProcedimento.CONSULTA ->
            "Concluído • ${procedimento.data}"
    }
}

private fun corDoProcedimento(
    tipo: TipoProcedimento
): Color {
    return when (tipo) {
        TipoProcedimento.VACINA ->
            Color(0xFFE76F45)

        TipoProcedimento.VERMIFUGO ->
            Color(0xFFA8D5A2)

        TipoProcedimento.CONSULTA ->
            Color(0xFFE4D7C4)
    }
}
