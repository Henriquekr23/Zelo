package com.example.zelo.demo.whatsapp

import com.example.zelo.model.StatusAgendamento
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

enum class DemoScenario(val title: String, val description: String) {
    NORMAL("Atendimento normal", "Tutor autorizado e cuidado próximo do vencimento."),
    NO_CONSENT("Tutor sem consentimento", "O envio deve ser bloqueado."),
    NO_RECURRENCE("Procedimento sem recorrência", "Não há vencimento automático válido."),
    CANCELLED("Agendamento cancelado", "Preservar o vencimento, sem confirmar atendimento cancelado."),
    FAILURE("Falha no envio", "Avance o relógio para tentar novamente ou demonstrar falha persistente."),
    REVOKED("Revogação por PARAR", "Envie o aviso e simule a revogação da autorização."),
    INACTIVE("Tutor inativo", "O pet precisa de um responsável ativo para receber lembretes.")
}

enum class DemoDelivery(val label: String) {
    READY("Aguardando geração do lembrete"),
    SENT("Enviado — simulação"),
    RETRY("Falha temporária — nova tentativa programada"),
    FAILED("Falha persistente — recepção deve verificar"),
    BLOCKED("Envio bloqueado"),
    REVOKED("Consentimento revogado — novos envios bloqueados")
}

enum class DemoAttendance(val label: String, val backendValue: String) {
    PENDING("Aguardando confirmação", "pending"),
    CONFIRMED("Confirmado", "confirmed"),
    RESCHEDULE("Remarcação solicitada", "reschedule_requested"),
    CANCELLED("Cancelado", "cancelled")
}

enum class DemoReply(val label: String) {
    CONFIRM("Confirmar"), RESCHEDULE("Remarcar"), CANCEL("Cancelar")
}

enum class DemoSpeaker { CLINIC, TUTOR }

data class DemoTutor(
    val name: String = "Carlos",
    val active: Boolean = true,
    // Fictitious format-only fixture. Never used by a network or messaging client.
    val phone: String = "+5500000000000",
    val consent: Boolean = true
)

data class DemoProcedure(
    val pet: String = "Thor",
    val description: String = "Vacina antirrábica",
    val lastVisit: Long,
    val intervalDays: Int?,
    val leadDays: Int = 7
) {
    val dueDate: Long?
        get() = intervalDays?.takeIf { it in 1..3650 }?.let { DemoDates.addDays(lastVisit, it) }
}

data class DemoMessage(val id: String, val speaker: DemoSpeaker, val text: String, val minute: Int)
data class DemoEvent(val order: Int, val minute: Int, val text: String)
data class DemoTicket(
    val id: Int, val reason: String, val originalMessage: String,
    val resolved: Boolean = false, val note: String = ""
)

data class DemoState(
    val scenario: DemoScenario,
    val referenceDate: Long,
    val tutor: DemoTutor,
    val procedure: DemoProcedure,
    val appointmentDate: Long,
    val appointmentStatus: StatusAgendamento = StatusAgendamento.AGENDADO,
    val attendance: DemoAttendance = DemoAttendance.PENDING,
    val delivery: DemoDelivery = DemoDelivery.READY,
    val minute: Int = 0,
    val attempts: Int = 0,
    val nextRetryMinute: Int? = null,
    val outboundId: String? = null,
    val consultationMessage: Boolean = false,
    val reply: DemoReply? = null,
    val lastInteraction: String = "Nenhuma resposta recebida",
    val feedback: String = "Simule o envio para começar.",
    val messages: List<DemoMessage> = emptyList(),
    val events: List<DemoEvent> = emptyList(),
    val tickets: List<DemoTicket> = emptyList(),
    val inputSequence: Int = 0,
    val processedInputs: Set<String> = emptySet(),
    val revocationChecked: Boolean = false
) {
    val canReply: Boolean get() = delivery == DemoDelivery.SENT &&
        consultationMessage && reply == null && tutor.consent &&
        appointmentStatus == StatusAgendamento.AGENDADO
    val openTickets: Int get() = tickets.count { !it.resolved }
}

object DemoDates {
    private val utc = TimeZone.getTimeZone("UTC")
    fun today(now: Long = System.currentTimeMillis()): Long {
        val local = Calendar.getInstance().apply { timeInMillis = now }
        return Calendar.getInstance(utc).apply {
            clear()
            set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
        }.timeInMillis
    }
    fun addDays(day: Long, days: Int): Long = Calendar.getInstance(utc).apply {
        timeInMillis = day
        add(Calendar.DAY_OF_MONTH, days)
    }.timeInMillis
    fun date(day: Long?): String = day?.let {
        SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).apply { timeZone = utc }.format(it)
    } ?: "Sem vencimento calculado"
    fun time(minute: Int): String = String.format(Locale.ROOT, "%02d:%02d", (9 + minute / 60) % 24, minute % 60)
}

object DemoFixtures {
    fun initial(scenario: DemoScenario, referenceDate: Long): DemoState {
        val interval = if (scenario == DemoScenario.NO_RECURRENCE) null else 30
        val procedure = DemoProcedure(lastVisit = DemoDates.addDays(referenceDate, 7 - 30), intervalDays = interval)
        return DemoState(
            scenario = scenario, referenceDate = referenceDate,
            tutor = DemoTutor(active = scenario != DemoScenario.INACTIVE,
                consent = scenario != DemoScenario.NO_CONSENT),
            procedure = procedure, appointmentDate = DemoDates.addDays(referenceDate, 7),
            appointmentStatus = if (scenario == DemoScenario.CANCELLED) StatusAgendamento.CANCELADO else StatusAgendamento.AGENDADO,
            attendance = if (scenario == DemoScenario.CANCELLED) DemoAttendance.CANCELLED else DemoAttendance.PENDING
        )
    }
}
