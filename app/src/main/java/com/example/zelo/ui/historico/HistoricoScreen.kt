package com.example.zelo.ui.historico

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.zelo.model.Pet
import com.example.zelo.model.Procedimento
import com.example.zelo.model.TipoProcedimento
import com.example.zelo.ui.theme.ZeloTheme
import com.example.zelo.viewmodel.HistoricoUiState

@Composable
fun HistoricoScreen(
    uiState: HistoricoUiState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        CabecalhoHistorico(uiState.petAtivo)

        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Histórico clínico",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.semantics { heading() }
            )

            Spacer(modifier = Modifier.height(ZeloTheme.spacing.lg))

            uiState.historico.forEachIndexed { indice, procedimento ->
                ProcedimentoHistoricoCard(
                    procedimento = procedimento,
                    primeiro = indice == 0,
                    ultimo = indice == uiState.historico.lastIndex
                )
            }
        }
    }
}

@Composable
private fun CabecalhoHistorico(pet: Pet) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 24.dp)
    ) {
        Text(
            text = "Histórico",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.semantics { heading() }
        )

        Spacer(modifier = Modifier.height(ZeloTheme.spacing.lg))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier.padding(ZeloTheme.spacing.lg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🐾",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.clearAndSetSemantics { }
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = ZeloTheme.spacing.md)
                ) {
                    Text(
                        text = pet.nome,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${pet.especie} • ${pet.raca}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ProcedimentoHistoricoCard(
    procedimento: Procedimento,
    primeiro: Boolean,
    ultimo: Boolean
) {
    val corMarcador = MaterialTheme.colorScheme.primary
    val corLinha = MaterialTheme.colorScheme.outlineVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Canvas(
            modifier = Modifier
                .width(16.dp)
                .fillMaxHeight()
        ) {
            val centro = Offset(size.width / 2, 24.dp.toPx())

            drawLine(
                color = corLinha,
                start = Offset(centro.x, if (primeiro) centro.y else 0f),
                end = Offset(centro.x, if (ultimo) centro.y else size.height),
                strokeWidth = 2.dp.toPx()
            )
            drawCircle(
                color = corMarcador,
                radius = 5.dp.toPx(),
                center = centro
            )
        }

        Spacer(modifier = Modifier.width(ZeloTheme.spacing.md))

        Card(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = ZeloTheme.spacing.lg)
                .semantics(mergeDescendants = true) { },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(ZeloTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(ZeloTheme.spacing.sm)
            ) {
                Text(
                    text = when (procedimento.tipo) {
                        TipoProcedimento.CONSULTA -> "Consulta"
                        TipoProcedimento.VACINA -> "Vacina"
                        TipoProcedimento.VERMIFUGO -> "Vermífugo"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = procedimento.descricao,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = procedimento.data,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
