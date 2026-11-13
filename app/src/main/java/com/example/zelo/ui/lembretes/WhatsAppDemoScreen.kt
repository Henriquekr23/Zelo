package com.example.zelo.ui.lembretes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zelo.demo.whatsapp.*
import com.example.zelo.demo.clinic.ClinicDemoData
import com.example.zelo.viewmodel.DemoTab
import com.example.zelo.viewmodel.WhatsAppDemoViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WhatsAppDemoScreen(model: WhatsAppDemoViewModel, onBack: () -> Unit) {
    val state = model.state
    val listState = rememberLazyListState()
    val tabOffset = with(LocalDensity.current) { 64.dp.roundToPx() }
    // Three compact overview items, sticky tabs, chat header, then message items.
    LaunchedEffect(model.tab, state.scenario, state.messages.size, state.events.isEmpty()) {
        if (state.events.isEmpty()) listState.scrollToItem(0)
        else if (model.tab == DemoTab.CHAT && state.messages.isNotEmpty()) {
            withFrameNanos { /* Measure the inserted bubble before moving the list. */ }
            listState.animateScrollToItem(4 + state.messages.size, -tabOffset)
        } else if (model.tab != DemoTab.CHAT) listState.animateScrollToItem(3)
    }
    var scenarios by remember { mutableStateOf(false) }
    var details by remember(state.scenario) { mutableStateOf(false) }
    var otherMessage by remember(state.scenario, state.outboundId, state.tutor.consent) { mutableStateOf(false) }
    var messageText by remember(state.scenario) { mutableStateOf("Preciso conversar com alguém sobre meu cachorro.") }
    var ticketId by remember(state.scenario) { mutableStateOf<Int?>(null) }
    var note by remember(state.scenario) { mutableStateOf("Tutor orientado pela recepção durante a simulação.") }
    val chatColors = conversationColors()

    fun reset() {
        otherMessage = false; scenarios = false; ticketId = null; details = false
        messageText = "Preciso conversar com alguém sobre meu cachorro."
        note = "Tutor orientado pela recepção durante a simulação."
        model.reset()
    }

    Scaffold(
        topBar = {
            Surface(Modifier.fillMaxWidth()) {
                Column(Modifier.statusBarsPadding().padding(horizontal = 12.dp, vertical = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onBack) { Text("Voltar") }
                        Column {
                            Text("Lembretes em ação", style = MaterialTheme.typography.titleLarge)
                            Text("MODO DEMONSTRAÇÃO", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text("Simulação educativa. Nenhuma mensagem real será enviada.",
                        style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp))
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
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).imePadding(),
            state = listState, contentPadding = PaddingValues(
                start = 12.dp, end = 12.dp, top = 8.dp, bottom = 12.dp)) {
            item(key = "scenario") {
                OutlinedCard(onClick = { scenarios = true }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Selecionar cenário", style = MaterialTheme.typography.labelSmall)
                            Text(state.scenario.title, color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        Text("⌄", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
            item(key = "procedure") {
                Card(Modifier.fillMaxWidth().padding(bottom = 8.dp).animateContentSize()) {
                    Column(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("${state.procedure.pet} • ${state.tutor.name}", fontWeight = FontWeight.Bold)
                                Text(state.procedure.description, style = MaterialTheme.typography.bodyMedium)
                            }
                            TextButton(onClick = { details = !details }) { Text(if (details) "Ocultar" else "Detalhes") }
                        }
                        Text("Vencimento: ${DemoDates.date(state.procedure.dueDate)}", style = MaterialTheme.typography.bodySmall)
                        if (details) {
                            DemoField("Tutor responsável", "${state.tutor.name} • ${if (state.tutor.active) "Ativo" else "Inativo"}")
                            DemoField("Data da simulação", DemoDates.date(state.referenceDate))
                            DemoField("Último atendimento", DemoDates.date(state.procedure.lastVisit))
                            DemoField("Recorrência", state.procedure.intervalDays?.let { "$it dias" } ?: "Sem recorrência válida")
                            DemoField("Antecedência", "${state.procedure.leadDays} dias")
                            DemoField("Consentimento", if (state.tutor.consent) "Autorizado na simulação" else "Não autorizado")
                        }
                    }
                }
            }
            item(key = "summary") {
                val warning = state.delivery in listOf(DemoDelivery.BLOCKED, DemoDelivery.FAILED)
                Surface(Modifier.fillMaxWidth().padding(bottom = 8.dp), shape = MaterialTheme.shapes.medium,
                    color = if (warning) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer) {
                    Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row {
                            Text(reminderSummary(state), Modifier.weight(1f),
                                style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Text(DemoDates.time(state.minute), style = MaterialTheme.typography.labelSmall)
                        }
                        Text("Agendamento: ${state.appointmentStatus.name}", style = MaterialTheme.typography.bodySmall)
                        Text("Comparecimento: ${state.attendance.label}", style = MaterialTheme.typography.bodySmall)
                        if (warning || state.delivery == DemoDelivery.REVOKED) {
                            Text(state.feedback, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            stickyHeader(key = "tabs") {
                TabRow(selectedTabIndex = model.tab.ordinal, containerColor = MaterialTheme.colorScheme.surface) {
                    DemoTab.entries.forEach { tab ->
                        Tab(selected = model.tab == tab, onClick = { model.selectTab(tab) },
                            modifier = Modifier.height(64.dp)) {
                            // Custom content avoids Tab's 16dp text padding splitting labels on 360dp phones.
                            Column(Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(tab.title, fontSize = 12.sp, maxLines = 1, softWrap = false,
                                    fontWeight = if (model.tab == tab) FontWeight.Bold else FontWeight.Normal)
                                if (tab == DemoTab.RECEPTION && state.openTickets > 0) {
                                    Text("${state.openTickets}", style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
            when (model.tab) {
                DemoTab.CHAT -> {
                    item(key = "chat-header") { ConversationHeader(chatColors) }
                    if (state.messages.isEmpty()) item(key = "waiting") {
                        Text("Aguardando geração do lembrete.", modifier = Modifier.fillMaxWidth()
                            .background(chatColors.background).padding(20.dp), color = chatColors.secondaryText)
                    }
                    items(state.messages, key = { it.id }) { message ->
                        DemoBubble(message, state, chatColors) { reply ->
                            model.dispatch(DemoCommand.Reply(requireNotNull(state.outboundId), reply))
                        }
                    }
                    item(key = "chat-controls") {
                        Column(Modifier.fillMaxWidth().background(chatColors.background).padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (state.delivery == DemoDelivery.READY) {
                                Button(onClick = { model.dispatch(DemoCommand.Send) }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Simular envio automático")
                                }
                            }
                            if (state.delivery == DemoDelivery.RETRY) {
                                Text("Tentativa ${state.attempts}/${DemoEngine.MAX_ATTEMPTS}. Próxima: ${DemoDates.time(requireNotNull(state.nextRetryMinute))}.",
                                    color = chatColors.text)
                                Button(onClick = { model.dispatch(DemoCommand.Retry(state.attempts, true)) },
                                    modifier = Modifier.fillMaxWidth()) { Text("Nova tentativa com sucesso") }
                                OutlinedButton(onClick = { model.dispatch(DemoCommand.Retry(state.attempts, false)) },
                                    modifier = Modifier.fillMaxWidth()) { Text("Simular nova falha") }
                            }
                            if (state.outboundId != null && state.tutor.consent) {
                                OutlinedButton(onClick = { otherMessage = true }, modifier = Modifier.fillMaxWidth()) { Text("Enviar outra mensagem") }
                                TextButton(onClick = { model.dispatch(DemoCommand.Text(state.outboundId, state.inputSequence, "PARAR")) }) {
                                    Text("Responder PARAR")
                                }
                            }
                            if (state.delivery == DemoDelivery.REVOKED) {
                                OutlinedButton(onClick = { model.dispatch(DemoCommand.CheckRevocation) },
                                    enabled = !state.revocationChecked, modifier = Modifier.fillMaxWidth()) { Text("Verificar novo envio") }
                            }
                            if (state.openTickets > 0) {
                                TextButton(onClick = { model.selectTab(DemoTab.RECEPTION) }) { Text("Ver pendências da recepção") }
                            }
                        }
                    }
                }
                DemoTab.AGENDA -> item { Box(Modifier.padding(top = 12.dp)) { DemoAppointment(state, compact = false) } }
                DemoTab.RECEPTION -> {
                    item {
                        Column(Modifier.padding(vertical = 12.dp)) {
                            Text("Recepção simulada", style = MaterialTheme.typography.titleMedium)
                            Text("Solicitações aguardam análise humana; nenhuma data é alterada automaticamente.",
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    if (state.tickets.isEmpty()) item { Text("Nenhuma pendência nesta demonstração.") }
                    items(state.tickets, key = { "ticket:${it.id}" }) { ticket ->
                        Card(Modifier.fillMaxWidth().padding(bottom = 8.dp).animateContentSize()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Pendência #${ticket.id}", fontWeight = FontWeight.Bold)
                                Text(ticket.reason)
                                Text("Mensagem original: ${ticket.originalMessage}", style = MaterialTheme.typography.bodySmall)
                                Text(if (ticket.resolved) "Resolvida" else "Aguardando recepção", color = MaterialTheme.colorScheme.primary)
                                if (ticket.resolved) Text("Observação: ${ticket.note}")
                                else TextButton(onClick = { ticketId = ticket.id }) { Text("Resolver pendência") }
                            }
                        }
                    }
                }
                DemoTab.HISTORY -> {
                    item {
                        Column(Modifier.padding(vertical = 12.dp)) {
                            Text("Histórico da demonstração", style = MaterialTheme.typography.titleMedium)
                            Text("Somente ações executadas nesta sessão.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    if (state.events.isEmpty()) item { Text("Nenhum evento registrado.") }
                    items(state.events, key = { "event:${it.order}" }) { event ->
                        Row(Modifier.padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(DemoDates.time(event.minute), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
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


private data class ConversationColors(
    val background: Color, val header: Color, val clinic: Color, val tutor: Color,
    val text: Color, val secondaryText: Color, val accent: Color
)

@Composable
private fun conversationColors(): ConversationColors =
    if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) {
        ConversationColors(Color(0xFF111E19), Color(0xFF153E31), Color(0xFF263932), Color(0xFF22513D),
            Color(0xFFEBF4EE), Color(0xFFB5CABD), Color(0xFF9AE0B7))
    } else {
        ConversationColors(Color(0xFFF1EEE6), Color(0xFF22654F), Color.White, Color(0xFFD9F3DF),
            Color(0xFF20372C), Color(0xFF5C7064), Color(0xFF236348))
    }

private fun reminderSummary(state: DemoState): String = when {
    state.delivery == DemoDelivery.REVOKED -> "Consentimento revogado"
    state.reply == DemoReply.CONFIRM -> "Comparecimento confirmado"
    state.reply == DemoReply.CANCEL -> "Agendamento cancelado"
    state.reply == DemoReply.RESCHEDULE -> "Remarcação solicitada"
    else -> state.delivery.label
}

@Composable
private fun ConversationHeader(colors: ConversationColors) {
    Surface(color = colors.header, contentColor = Color.White) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.15f)) {
                Text("🐾", Modifier.padding(10.dp), fontSize = 20.sp)
            }
            Column {
                Text(ClinicDemoData.NAME, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("Conversa simulada", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun DemoBubble(
    message: DemoMessage, state: DemoState, colors: ConversationColors, onReply: (DemoReply) -> Unit
) {
    val tutorMessage = message.speaker == DemoSpeaker.TUTOR
    val transition = remember(message.id) { MutableTransitionState(false).apply { targetState = true } }
    Box(Modifier.fillMaxWidth().background(colors.background).padding(horizontal = 10.dp, vertical = 5.dp)) {
        AnimatedVisibility(visibleState = transition, enter = fadeIn()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = if (tutorMessage) Arrangement.End else Arrangement.Start) {
                Surface(Modifier.fillMaxWidth(0.93f), shape = MaterialTheme.shapes.medium,
                    color = if (tutorMessage) colors.tutor else colors.clinic, contentColor = colors.text) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(message.text, style = MaterialTheme.typography.bodyMedium)
                        val indicator = when {
                            tutorMessage -> "Recebido"
                            message.id == state.outboundId -> "Enviado"
                            else -> "Respondido"
                        }
                        Text("${DemoDates.time(message.minute)} • $indicator • simulação",
                            Modifier.align(Alignment.End), style = MaterialTheme.typography.labelSmall, color = colors.secondaryText)
                        if (message.id == state.outboundId && state.consultationMessage) {
                            HorizontalDivider(color = colors.secondaryText.copy(alpha = 0.2f))
                            DemoReply.entries.forEach { reply ->
                                val selected = state.reply == reply
                                OutlinedButton(onClick = { onReply(reply) }, enabled = state.canReply,
                                    modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small,
                                    border = BorderStroke(1.dp, colors.accent.copy(alpha = 0.3f)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = colors.accent,
                                        disabledContainerColor = if (selected) colors.tutor else Color.Transparent,
                                        disabledContentColor = if (selected) colors.accent else colors.secondaryText)) {
                                    Text(if (selected) "✓ ${reply.label}" else reply.label)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
