package com.example.zelo.ui.lembretes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.zelo.viewmodel.WhatsAppViewModel

@Composable
fun WhatsAppScreen(model: WhatsAppViewModel, onBack: () -> Unit, onOpenDemo: () -> Unit = {}) {
    var confirmServer by remember { mutableStateOf(false) }
    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick = onBack) { Text("Voltar") }
            Text("Lembretes pelo WhatsApp", style = MaterialTheme.typography.headlineSmall)
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Modo Demonstração", style = MaterialTheme.typography.titleMedium)
                    Text("Experimente o fluxo completo sem internet, credenciais ou mensagens reais.")
                    Button(onClick = onOpenDemo) { Text("Experimentar demonstração") }
                }
            }
            Text("Área do piloto para um atendente autorizado. Ao sincronizar, os pets e agendamentos deste aparelho serão vinculados ao tutor abaixo e à clínica conectada.")
            OutlinedTextField(value = model.url, onValueChange = { model.url = it },
                label = { Text("Endereço HTTPS da clínica") }, singleLine = true, enabled = !model.busy)
            OutlinedTextField(value = model.token, onValueChange = { model.token = it },
                label = { Text("Credencial pessoal da clínica") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(), enabled = !model.busy)
            OutlinedTextField(value = model.name, onValueChange = { model.name = it },
                label = { Text("Tutor responsável") }, singleLine = true, enabled = !model.busy)
            OutlinedTextField(value = model.phone, onValueChange = { model.phone = it },
                label = { Text("WhatsApp com código do país") },
                placeholder = { Text("+5511999999999") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), enabled = !model.busy)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Checkbox(checked = model.consent, onCheckedChange = { model.consent = it }, enabled = !model.busy)
                Text("O tutor autoriza receber lembretes desta clínica pelo WhatsApp.")
            }
            OutlinedTextField(value = model.evidence, onValueChange = { model.evidence = it },
                label = { Text("Origem da autorização") },
                supportingText = { Text("Ex.: formulário autorizado pelo tutor em 12/11/2026.") },
                enabled = !model.busy)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Receber lembretes")
                Switch(checked = model.enabled, onCheckedChange = { model.enabled = it },
                    enabled = !model.busy && model.connected && model.provider != "disabled")
            }
            Text("Para revogar a autorização, desmarque o consentimento e sincronize. Sem conexão, a revogação fica pendente; o tutor também pode responder PARAR pelo WhatsApp.")
            Button(onClick = { model.saveLocal() }, enabled = !model.busy) { Text("Salvar no aparelho") }
            Button(onClick = { model.sync() }, enabled = !model.busy) { Text("Sincronizar com a clínica") }
            OutlinedButton(onClick = { confirmServer = true }, enabled = !model.busy) {
                Text("Resolver conflito usando dados da clínica")
            }
            if (model.busy) CircularProgressIndicator()
            Text(model.status, color = MaterialTheme.colorScheme.primary)
            Text("A clínica configura os procedimentos recorrentes. Confirmar comparecimento não conclui o procedimento. Remarcações dependem da recepção.")
            val reminders = model.history?.optJSONArray("reminders")
            Text("Lembretes da clínica", style = MaterialTheme.typography.titleMedium)
            if (reminders == null) Text("Nenhum histórico consultado nesta sessão.")
            else if (reminders.length() == 0) Text("Nenhum lembrete registrado.")
            else for (i in (reminders.length() - 1 downTo 0).take(10)) {
                val reminder = reminders.getJSONObject(i)
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Vencimento: ${reminder.getString("due_on")}")
                        Text("Envio: ${deliveryLabel(reminder.getString("state"))}")
                        Text("Resposta: ${replyLabel(reminder.optString("response"))}")
                        if (!reminder.isNull("error")) Text("Falha: ${reminder.getString("error")}")
                    }
                }
            }
        }
    }
    if (confirmServer) AlertDialog(
        onDismissRequest = { confirmServer = false },
        title = { Text("Usar dados da clínica?") },
        text = { Text("As alterações locais pendentes dos registros já vinculados serão substituídas pelos dados atuais da clínica. Registros locais ainda não enviados serão preservados.") },
        confirmButton = { TextButton(onClick = { confirmServer = false; model.sync(true) }) { Text("Usar dados da clínica") } },
        dismissButton = { TextButton(onClick = { confirmServer = false }) { Text("Voltar") } }
    )
}

private fun deliveryLabel(value: String) = when (value) {
    "pending" -> "Programado"
    "retry" -> "Aguardando nova tentativa"
    "sending" -> "Solicitando envio"
    "sent" -> "Aceito pela Meta; entrega ainda não confirmada"
    "delivered" -> "Entregue"
    "read" -> "Lido"
    "simulated" -> "Simulado; sem envio real"
    "suppressed" -> "Envio suspenso"
    "missed" -> "Janela de envio perdida"
    "unknown" -> "Resultado incerto; recepção deve verificar"
    "failed" -> "Falha; recepção deve verificar"
    else -> value
}

private fun replyLabel(value: String) = when (value) {
    "confirm" -> "Comparecimento confirmado"
    "reschedule" -> "Remarcação solicitada"
    "cancel" -> "Cancelamento solicitado"
    else -> "Sem resposta"
}
