package com.example.zelo.demo.whatsapp

import com.example.zelo.model.StatusAgendamento

sealed interface DemoCommand {
    data object Send : DemoCommand
    data class Retry(val expectedAttempt: Int, val succeed: Boolean) : DemoCommand
    data class Reply(val messageId: String, val action: DemoReply) : DemoCommand
    data class Text(val messageId: String, val sequence: Int, val text: String) : DemoCommand
    data class Resolve(val ticketId: Int, val note: String) : DemoCommand
    data object CheckRevocation : DemoCommand
}

/** Pure, in-memory state machine: no Android Context, Room, preferences, provider or HTTP. */
class DemoEngine {
    companion object {
        const val MAX_ATTEMPTS = 3
        const val RETRY_MINUTES = 2
    }

    fun reduce(state: DemoState, command: DemoCommand): DemoState = when (command) {
        DemoCommand.Send -> if (state.delivery == DemoDelivery.READY) attempt(state, true) else state
        is DemoCommand.Retry -> if (state.delivery == DemoDelivery.RETRY && state.attempts == command.expectedAttempt) {
            attempt(state.copy(minute = requireNotNull(state.nextRetryMinute)), command.succeed)
        } else state
        is DemoCommand.Reply -> reply(state, command)
        is DemoCommand.Text -> text(state, command)
        is DemoCommand.Resolve -> resolve(state, command)
        DemoCommand.CheckRevocation -> if (!state.tutor.consent && state.delivery == DemoDelivery.REVOKED && !state.revocationChecked) {
            event(state.copy(revocationChecked = true), "Nova verificação: envio bloqueado por consentimento revogado.")
                .copy(feedback = "O tutor não autorizou novos lembretes. Nenhuma nova mensagem foi enviada.")
        } else state
    }

    private fun event(state: DemoState, text: String): DemoState {
        val minute = state.minute + 1
        return state.copy(minute = minute, events = state.events + DemoEvent(state.events.size + 1, minute, text))
    }

    private fun blocked(state: DemoState, reason: String): DemoState =
        event(state, reason).copy(delivery = DemoDelivery.BLOCKED, feedback = reason, nextRetryMinute = null)

    private fun attempt(original: DemoState, retrySucceeds: Boolean): DemoState {
        val procedure = original.procedure
        val due = procedure.dueDate ?: return blocked(original, "Procedimento sem recorrência válida. Nenhum lembrete automático foi gerado.")
        var state = original
        if (state.attempts == 0) {
            state = event(state, "Procedimento recorrente identificado: intervalo de ${procedure.intervalDays} dias.")
            state = event(state, "Vencimento calculado: ${DemoDates.date(due)}.")
        }
        if (!state.tutor.active) return blocked(state, "Tutor inativo. O pet precisa de um responsável ativo.")
        if (!Regex("^\\+[1-9][0-9]{7,14}$").matches(state.tutor.phone)) {
            return blocked(state, "O tutor não possui telefone em formato válido.")
        }
        if (!state.tutor.consent) return blocked(state, "O tutor não autorizou o recebimento de lembretes.")
        if (procedure.leadDays !in 0..90 || due < state.referenceDate ||
            due > DemoDates.addDays(state.referenceDate, procedure.leadDays)) {
            return blocked(state, "O vencimento está fora da janela de antecedência configurada.")
        }
        if (state.attempts == 0) {
            state = event(state, "Tutor ativo, telefone e consentimento validados.")
            state = event(state, "Lembrete gerado dentro da janela de ${procedure.leadDays} dias.")
        }
        val consultation = state.appointmentStatus == StatusAgendamento.AGENDADO && state.appointmentDate == due
        if (state.attempts == 0 && !consultation) {
            state = event(state, "Vencimento clínico preservado; atendimento cancelado ou sem horário válido não terá confirmação.")
        }
        state = event(state.copy(attempts = state.attempts + 1), "Tentativa simulada ${state.attempts + 1} iniciada.")
        val fails = (state.scenario == DemoScenario.FAILURE && state.attempts == 1) || !retrySucceeds
        if (fails) {
            state = event(state, "Falha transitória simulada. Nenhuma mensagem foi enviada.")
            if (state.attempts >= MAX_ATTEMPTS) {
                state = ticket(state, "Falhas persistentes no envio", "$MAX_ATTEMPTS tentativas simuladas falharam.")
                return state.copy(delivery = DemoDelivery.FAILED, nextRetryMinute = null,
                    feedback = "Falha persistente. Pendência criada para a recepção.")
            }
            val next = state.minute + RETRY_MINUTES * (1 shl (state.attempts - 1))
            return event(state, "Nova tentativa programada para ${DemoDates.time(next)} no relógio da demonstração.")
                .copy(delivery = DemoDelivery.RETRY, nextRetryMinute = next,
                    feedback = "Falha temporária. Avance a simulação para a nova tentativa.")
        }
        val id = "demo:${state.scenario.name}:${state.referenceDate}"
        val message = "Olá, ${state.tutor.name}!\n\nA ${procedure.description.lowercase()} de ${procedure.pet} está prevista para ${DemoDates.date(due)}." +
            if (consultation) "\n\nPara organizar o atendimento, escolha Confirmar, Remarcar ou Cancelar."
            else "\n\nO vencimento clínico continua válido. Procure a recepção para organizar o próximo cuidado. Este aviso não confirma um atendimento cancelado."
        state = event(state, "Envio simulado registrado. Nenhuma API ou WhatsApp real foi utilizado.")
        return state.copy(delivery = DemoDelivery.SENT, nextRetryMinute = null, outboundId = id,
            consultationMessage = consultation,
            messages = state.messages + DemoMessage(id, DemoSpeaker.CLINIC, message, state.minute),
            feedback = if (consultation) "Lembrete enviado — simulação. Escolha uma resposta."
                else "Aviso clínico enviado — simulação, sem botões de comparecimento.")
    }

    private fun reply(original: DemoState, command: DemoCommand.Reply): DemoState {
        if (!original.canReply || original.outboundId != command.messageId) return original
        var state = event(original, "Resposta simulada recebida: ${command.action.label}.")
        state = state.copy(reply = command.action, lastInteraction = command.action.label,
            messages = state.messages + DemoMessage("reply:${command.messageId}", DemoSpeaker.TUTOR, command.action.label, state.minute))
        val result = when (command.action) {
            DemoReply.CONFIRM -> {
                state = state.copy(attendance = DemoAttendance.CONFIRMED)
                state = event(state, "Confirmação registrada; estado clínico permanece AGENDADO.")
                "Comparecimento confirmado. O procedimento ainda não foi concluído."
            }
            DemoReply.RESCHEDULE -> {
                state = state.copy(attendance = DemoAttendance.RESCHEDULE)
                state = ticket(state, "Remarcação solicitada", command.action.label)
                state = event(state, "Data do agendamento preservada; recepção deve verificar disponibilidade.")
                "Solicitação encaminhada à recepção. A data ainda não foi alterada."
            }
            DemoReply.CANCEL -> {
                state = state.copy(appointmentStatus = StatusAgendamento.CANCELADO, attendance = DemoAttendance.CANCELLED)
                state = event(state, "Agendamento atualizado para CANCELADO; registro e vencimento clínico preservados.")
                "Agendamento cancelado. O histórico foi preservado."
            }
        }
        return state.copy(feedback = result, messages = state.messages +
            DemoMessage("result:${command.messageId}", DemoSpeaker.CLINIC, result, state.minute))
    }

    private fun text(original: DemoState, command: DemoCommand.Text): DemoState {
        val body = command.text
        val inputId = "${command.messageId}:input:${command.sequence}"
        if (original.outboundId != command.messageId ||
            command.sequence != original.inputSequence || body.isBlank() ||
            body.length > 1000 || inputId in original.processedInputs || !original.tutor.consent) return original
        var state = event(original, "Mensagem simulada recebida e preservada.")
        state = state.copy(lastInteraction = body, inputSequence = state.inputSequence + 1,
            processedInputs = state.processedInputs + inputId,
            messages = state.messages + DemoMessage(inputId, DemoSpeaker.TUTOR, body, state.minute))
        if (body.trim().equals("PARAR", true) || body.trim().equals("SAIR", true) || body.trim().equals("STOP", true)) {
            return event(state.copy(tutor = state.tutor.copy(consent = false)), "Consentimento revogado pelo tutor. Novos envios bloqueados.")
                .copy(delivery = DemoDelivery.REVOKED, feedback = "Consentimento revogado. Nenhum novo lembrete será enviado.")
        }
        state = event(state, "Resposta não reconhecida; nenhum agendamento foi alterado.")
        return ticket(state, "Atendimento humano necessário", body)
            .copy(feedback = "Atendimento humano necessário. Mensagem encaminhada à recepção.")
    }

    private fun ticket(state: DemoState, reason: String, originalMessage: String): DemoState =
        event(state, "Pendência criada: $reason.").let {
            it.copy(tickets = it.tickets + DemoTicket(it.tickets.size + 1, reason, originalMessage))
        }

    private fun resolve(state: DemoState, command: DemoCommand.Resolve): DemoState {
        val ticket = state.tickets.firstOrNull { it.id == command.ticketId } ?: return state
        val note = command.note.trim()
        if (ticket.resolved || note.isBlank() || note.length > 1000) return state
        return event(state, "Recepção resolveu a pendência #${ticket.id}: $note").copy(
            tickets = state.tickets.map { if (it.id == ticket.id) it.copy(resolved = true, note = note) else it },
            feedback = "Pendência resolvida na simulação. Observação preservada no histórico.")
    }
}
