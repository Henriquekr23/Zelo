package com.example.zelo.ui.lembretes

import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.zelo.model.Pet
import com.example.zelo.model.Procedimento
import com.example.zelo.model.TipoProcedimento
import com.example.zelo.ui.theme.ZeloTheme
import com.example.zelo.viewmodel.LembretesUiState

@Composable
fun LembretesScreen(
    uiState: LembretesUiState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        CabecalhoLembretes(
            pet = uiState.pet
        )

        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Próximos vencimentos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.semantics {
                    heading()
                }
            )

            Spacer(
                modifier = Modifier.height(
                    ZeloTheme.spacing.lg
                )
            )

            uiState.vencimentos.forEach { procedimento ->
                VencimentoCard(
                    procedimento = procedimento
                )

                Spacer(
                    modifier = Modifier.height(
                        ZeloTheme.spacing.md
                    )
                )
            }
        }
    }
}

@Composable
private fun CabecalhoLembretes(
    pet: Pet
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.primary
            )
            .statusBarsPadding()
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 20.dp,
                bottom = 24.dp
            )
    ) {
        Text(
            text = "Lembretes",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.semantics {
                heading()
            }
        )

        Spacer(
            modifier = Modifier.height(
                ZeloTheme.spacing.lg
            )
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier.padding(
                    ZeloTheme.spacing.lg
                ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            MaterialTheme
                                .colorScheme
                                .secondaryContainer
                        ),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        text = "🐾",
                        style =
                            MaterialTheme
                                .typography
                                .titleLarge,
                        modifier =
                            Modifier.clearAndSetSemantics { }
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(
                            start =
                                ZeloTheme.spacing.md
                        )
                ) {
                    Text(
                        text = pet.nome,
                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        text =
                            "${pet.especie} • ${pet.raca}",
                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun VencimentoCard(
    procedimento: Procedimento
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(
                mergeDescendants = true
            ) { },
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(
                ZeloTheme.spacing.lg
            ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme
                            .colorScheme
                            .secondaryContainer
                    ),
                contentAlignment =
                    Alignment.Center
            ) {
                Text(
                    text = iconeProcedimento(
                        procedimento.tipo
                    ),
                    style =
                        MaterialTheme
                            .typography
                            .titleLarge,
                    modifier =
                        Modifier.clearAndSetSemantics { }
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start =
                            ZeloTheme.spacing.md
                    ),
                verticalArrangement =
                    Arrangement.spacedBy(
                        ZeloTheme.spacing.sm
                    )
            ) {
                Text(
                    text =
                        procedimento.descricao,
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,
                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text = nomeProcedimento(
                        procedimento.tipo
                    ),
                    style =
                        MaterialTheme
                            .typography
                            .labelLarge,
                    color =
                        MaterialTheme
                            .colorScheme
                            .primary
                )

                Text(
                    text = procedimento.data,
                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            }

            Text(
                text = "EM BREVE",
                style =
                    MaterialTheme
                        .typography
                        .labelSmall,
                fontWeight =
                    FontWeight.Bold,
                color =
                    MaterialTheme
                        .colorScheme
                        .primary
            )
        }
    }
}

private fun nomeProcedimento(
    tipo: TipoProcedimento
): String {
    return when (tipo) {
        TipoProcedimento.VACINA ->
            "Vacina"

        TipoProcedimento.VERMIFUGO ->
            "Vermífugo"

        TipoProcedimento.CONSULTA ->
            "Consulta"
    }
}

private fun iconeProcedimento(
    tipo: TipoProcedimento
): String {
    return when (tipo) {
        TipoProcedimento.VACINA ->
            "💉"

        TipoProcedimento.VERMIFUGO ->
            "💊"

        TipoProcedimento.CONSULTA ->
            "🩺"
    }
}