package com.example.zelo.ui.lembretes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.zelo.demo.whatsapp.*
import com.example.zelo.viewmodel.DemoTab
import com.example.zelo.viewmodel.WhatsAppDemoViewModel

@Composable
fun WhatsAppDemoScreen(model: WhatsAppDemoViewModel, onBack: () -> Unit) {
    val state = model.state
    val listState = rememberLazyListState()
    LaunchedEffect(model.tab, state.scenario, state.messages.size, state.events.isEmpty()) {
        if (state.events.isEmpty()) listState.scrollToItem(0)
        else if (model.tab == DemoTab.CHAT && state.messages.isNotEmpty()) {
            listState.animateScrollToItem(5 + state.messages.size)
        } else if (model.tab != DemoTab.CHAT) listState.animateScrollToItem(4)
    }
    var scenarios by remember { mutableStateOf(false) }
    var details by remember(state.scenario) { mutableStateOf(false) }
    var otherMessage by remember(state.scenario, state.outboundId, state.tutor.consent) { mutableStateOf(false) }
    var messageText by remember(state.scenario) { mutableStateOf("Preciso conversar com alguém sobre meu cachorro.") }
    var ticketId by remember(state.scenario) { mutableStateOf<Int?>(null) }
    var note by remember(state.scenario) { mutableStateOf("Tutor orientado pela recepção durante a simulação.") }

    // Restarting also dismisses drafts/dialogs; only fictitious in-memory state is reset.
    fun reset() {
        otherMessage = false
        scenarios = false
        ticketId = null
        details = false
        messageText = "Preciso conversar com alguém sobre meu cachorro."
        note = "Tutor orientado pela recepção durante a simulação."
        model.reset()
    }

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onBack) { Text("Voltar") }
                        Spacer(Modifier.weight(1f))
                        Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.small) {
                            Text("MODO DEMONSTRAÇÃO", Modifier.padding(8.dp),
                                style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text("Lembretes em ação", style = MaterialTheme.typography.headlineSmall)
                    Text("Simulação educativa. Nenhuma mensagem real será enviada.",
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(Modifier.navigationBarsPadding().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { reset() }, modifier = Modifier.weight(1f)) {
                        Text("Reiniciar demonstração", style = MaterialTheme.typography.labelMedium)
                    }
                    Button(onClick = { model.nextStep() }, modifier = Modifier.weight(1f)) { Text("Próximo passo") }
                }
            }
        }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).imePadding(), state = listState,
            contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                OutlinedCard(onClick = { scenarios = true }, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Selecionar cenário", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary)
                        Text(state.scenario.title, fontWeight = FontWeight.Bold)
                        Text(state.scenario.description, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            item {
                Card(Modifier.fillMaxWidth().animateContentSize()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("${state.procedure.pet} • ${state.tutor.name}", fontWeight = FontWeight.Bold)
                                Text(state.procedure.description, style = MaterialTheme.typography.bodyMedium)
                            }
                            TextButton(onClick = { details = !details }) { Text(if (details) "Ocultar" else "Detalhes") }
                        }
                        Text("Vencimento: ${DemoDates.date(state.procedure.dueDate)}")
                        if (details) {
                            DemoField("Tutor responsável", "${state.tutor.name} • ${if (state.tutor.active) "Ativo" else "Inativo"}")
                            DemoField("Data da simulação", DemoDates.date(state.referenceDate))
                            DemoField("Último atendimento", DemoDates.date(state.procedure.lastVisit))
                            DemoField("Recorrência", state.procedure.intervalDays?.let { "$it dias" } ?: "Sem recorrência válida")
                            DemoField("Antecedência", "${state.procedure.leadDays} dias")
                            DemoField("Consentimento", if (state.tutor.consent) "Autorizado na simulação" else "Não autorizado")
                            DemoField("Agendamento", state.appointmentStatus.name)
                        }
                    }
                }
            }
            item { DemoAppointment(state, compact = true) }
            item {
                Surface(shape = MaterialTheme.shapes.medium,
                    color = if (state.delivery in listOf(DemoDelivery.BLOCKED, DemoDelivery.FAILED))
                        MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer) {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                        Text(state.delivery.label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Text(state.feedback, style = MaterialTheme.typography.bodySmall)
                        Text("Relógio simulado: ${DemoDates.time(state.minute)}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            item {
                ScrollableTabRow(selectedTabIndex = model.tab.ordinal, edgePadding = 0.dp) {
                    DemoTab.entries.forEach { tab ->
                        Tab(selected = model.tab == tab, onClick = { model.selectTab(tab) },
                            text = { Text(tab.title + if (tab == DemoTab.RECEPTION && state.openTickets > 0) " (${state.openTickets})" else "") })
                    }
                }
            }
            when (model.tab) {
                DemoTab.CHAT -> {
                    item {
                        Text("Clínica Jardim • fictícia", style = MaterialTheme.typography.titleMedium)
                        Text("Conversa simulada", style = MaterialTheme.typography.labelSmall)
                    }
                    if (state.messages.isEmpty()) item {
                        Text("Aguardando geração do lembrete.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    items(state.messages, key = { it.id }) { message -> DemoBubble(message) }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (state.delivery == DemoDelivery.READY) {
                                Button(onClick = { model.dispatch(DemoCommand.Send) }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Simular envio automático")
                                }
                            }
                            if (state.delivery == DemoDelivery.RETRY) {
                                Text("Tentativa ${state.attempts}/${DemoEngine.MAX_ATTEMPTS}. Próxima: ${DemoDates.time(requireNotNull(state.nextRetryMinute))}.")
                                Button(onClick = { model.dispatch(DemoCommand.Retry(state.attempts, true)) },
                                    modifier = Modifier.fillMaxWidth()) { Text("Nova tentativa com sucesso") }
                                OutlinedButton(onClick = { model.dispatch(DemoCommand.Retry(state.attempts, false)) },
                                    modifier = Modifier.fillMaxWidth()) { Text("Simular nova falha") }
                            }
                            if (state.canReply) {
                                Text("Responder como ${state.tutor.name}", style = MaterialTheme.typography.labelLarge)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    DemoReply.entries.forEach { reply ->
                                        FilledTonalButton(
                                            onClick = { model.dispatch(DemoCommand.Reply(requireNotNull(state.outboundId), reply)) },
                                            modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 6.dp, vertical = 12.dp)
                                        ) { Text(reply.label, style = MaterialTheme.typography.labelMedium) }
                                    }
                                }
                            }
                            if (state.outboundId != null && state.tutor.consent) {
                                OutlinedButton(onClick = { otherMessage = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Enviar outra mensagem")
                                }
                                TextButton(onClick = {
                                    model.dispatch(DemoCommand.Text(state.outboundId, state.inputSequence, "PARAR"))
                                }) { Text("Responder PARAR") }
                            }
                            if (state.delivery == DemoDelivery.REVOKED) {
                                OutlinedButton(onClick = { model.dispatch(DemoCommand.CheckRevocation) },
                                    enabled = !state.revocationChecked, modifier = Modifier.fillMaxWidth()) { Text("Verificar novo envio") }
                            }
                            if (state.openTickets > 0) {
                                TextButton(onClick = { model.selectTab(DemoTab.RECEPTION) }) {
                                    Text("Ver pendências da recepção")
                                }
                            }
                        }
                    }
                }
                DemoTab.AGENDA -> item { DemoAppointment(state, compact = false) }
                DemoTab.RECEPTION -> {
                    item {
                        Text("Recepção simulada", style = MaterialTheme.typography.titleMedium)
                        Text("Solicitações aguardam análise humana; nenhuma data é alterada automaticamente.",
                            style = MaterialTheme.typography.bodySmall)
                    }
                    if (state.tickets.isEmpty()) item { Text("Nenhuma pendência nesta demonstração.") }
                    items(state.tickets, key = { it.id }) { ticket ->
                        Card(Modifier.fillMaxWidth().animateContentSize()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Pendência #${ticket.id}", fontWeight = FontWeight.Bold)
                                Text(ticket.reason)
                                Text("Mensagem original: ${ticket.originalMessage}", style = MaterialTheme.typography.bodySmall)
                                Text(if (ticket.resolved) "Resolvida" else "Aguardando recepção",
                                    color = MaterialTheme.colorScheme.primary)
                                if (ticket.resolved) Text("Observação: ${ticket.note}")
                                else TextButton(onClick = { ticketId = ticket.id }) { Text("Resolver pendência") }
                            }
                        }
                    }
                }
                DemoTab.HISTORY -> {
                    item {
                        Text("Histórico da demonstração", style = MaterialTheme.typography.titleMedium)
                        Text("Somente ações executadas nesta sessão.", style = MaterialTheme.typography.bodySmall)
                    }
                    if (state.events.isEmpty()) item { Text("Nenhum evento registrado.") }
                    items(state.events, key = { it.order }) { event ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(DemoDates.time(event.minute), style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary)
                            Text("${event.order}. ${event.text}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }

    if (scenarios) AlertDialog(
        onDismissRequest = { scenarios = false },
        title = { Text("Cenários de teste") },
        text = {
            Column {
                DemoScenario.entries.forEach { scenario ->
                    TextButton(onClick = {
                        model.selectScenario(scenario)
                        scenarios = false
                        otherMessage = false
                        ticketId = null
                    }, modifier = Modifier.fillMaxWidth()) { Text(scenario.title) }
                }
            }
        },
        confirmButton = { TextButton(onClick = { scenarios = false }) { Text("Fechar") } }
    )
    if (otherMessage && state.outboundId != null) AlertDialog(
        onDismissRequest = { otherMessage = false },
        title = { Text("Mensagem fictícia do tutor") },
        text = { OutlinedTextField(value = messageText, onValueChange = { if (it.length <= 1000) messageText = it },
            label = { Text("Mensagem") }, minLines = 3) },
        confirmButton = { TextButton(enabled = messageText.isNotBlank(), onClick = {
            model.dispatch(DemoCommand.Text(state.outboundId, state.inputSequence, messageText))
            otherMessage = false
        }) { Text("Enviar na simulação") } },
        dismissButton = { TextButton(onClick = { otherMessage = false }) { Text("Voltar") } }
    )
    ticketId?.let { id ->
        AlertDialog(
            onDismissRequest = { ticketId = null },
            title = { Text("Resolver pendência simulada") },
            text = { OutlinedTextField(value = note, onValueChange = { if (it.length <= 1000) note = it },
                label = { Text("Observação da recepção") }, minLines = 3) },
            confirmButton = { TextButton(enabled = note.isNotBlank(), onClick = {
                model.dispatch(DemoCommand.Resolve(id, note))
                ticketId = null
            }) { Text("Registrar resolução") } },
            dismissButton = { TextButton(onClick = { ticketId = null }) { Text("Voltar") } }
        )
    }
}

@Composable
private fun DemoAppointment(state: DemoState, compact: Boolean) {
    OutlinedCard(Modifier.fillMaxWidth().animateContentSize()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (!compact) Text("Agenda da demonstração", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            DemoField("Agendamento", state.appointmentStatus.name)
            DemoField("Comparecimento", state.attendance.label)
            if (!compact) {
                DemoField("Procedimento", state.procedure.description)
                DemoField("Data prevista", DemoDates.date(state.appointmentDate))
                DemoField("Última interação", state.lastInteraction)
                Text("Confirmar não conclui o procedimento clínico.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun DemoField(label: String, value: String) {
    Text("$label: $value", style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun DemoBubble(message: DemoMessage) {
    val incoming = message.speaker == DemoSpeaker.TUTOR
    val transition = remember(message.id) { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(visibleState = transition, enter = fadeIn() + expandVertically()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = if (incoming) Arrangement.End else Arrangement.Start) {
            Surface(Modifier.fillMaxWidth(0.93f),
                shape = MaterialTheme.shapes.medium,
                color = if (incoming) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(if (incoming) "Tutor • simulação" else "Clínica • simulação",
                        style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text(message.text, style = MaterialTheme.typography.bodyMedium)
                    Text(DemoDates.time(message.minute) + if (incoming) " • Resposta simulada" else " • Enviado — simulação",
                        style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
