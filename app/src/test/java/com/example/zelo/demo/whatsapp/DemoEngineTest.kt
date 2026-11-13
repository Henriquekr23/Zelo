package com.example.zelo.demo.whatsapp

import com.example.zelo.model.StatusAgendamento
import com.example.zelo.viewmodel.DemoTab
import com.example.zelo.viewmodel.WhatsAppDemoViewModel
import com.example.zelo.data.local.ZeloDatabase
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class DemoEngineTest {
    private val engine = DemoEngine()
    private val reference = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear(); set(2028, Calendar.FEBRUARY, 23)
    }.timeInMillis
    private fun initial(scenario: DemoScenario = DemoScenario.NORMAL) = DemoFixtures.initial(scenario, reference)
    private fun sent(scenario: DemoScenario = DemoScenario.NORMAL) = engine.reduce(initial(scenario), DemoCommand.Send)
    private fun answer(state: DemoState, reply: DemoReply) =
        engine.reduce(state, DemoCommand.Reply(requireNotNull(state.outboundId), reply))
    private fun text(state: DemoState, body: String) =
        engine.reduce(state, DemoCommand.Text(requireNotNull(state.outboundId), state.inputSequence, body))

    @Test fun validReminderHasControlledDateAndSimulatedMessage() {
        val state = sent()
        assertEquals(DemoDelivery.SENT, state.delivery)
        assertEquals(1, state.attempts)
        assertTrue(state.canReply)
        assertEquals("01/03/2028", DemoDates.date(state.procedure.dueDate))
        assertEquals("31/01/2028", DemoDates.date(state.procedure.lastVisit))
        assertEquals(1, state.messages.size)
        assertTrue(state.messages.single().text.contains("Carlos"))
        assertTrue(state.messages.single().text.contains("Thor"))
        assertTrue(state.messages.single().text.contains("01/03/2028"))
    }

    @Test fun missingConsentBlocksWithoutSending() {
        val state = sent(DemoScenario.NO_CONSENT)
        assertEquals(DemoDelivery.BLOCKED, state.delivery)
        assertTrue(state.feedback.contains("não autorizou"))
        assertTrue(state.messages.isEmpty())
        assertEquals(0, state.attempts)
    }

    @Test fun invalidRecurrenceNeverGeneratesReminder() {
        for (interval in listOf(null, 0, -1, 3651)) {
            val base = initial()
            val result = engine.reduce(base.copy(procedure = base.procedure.copy(intervalDays = interval)), DemoCommand.Send)
            assertEquals(DemoDelivery.BLOCKED, result.delivery)
            assertTrue(result.messages.isEmpty())
            assertFalse(result.events.any { it.text.startsWith("Lembrete gerado") })
        }
    }

    @Test fun inactiveGuardianBlocksAccordingToRn003() {
        val state = sent(DemoScenario.INACTIVE)
        assertEquals(DemoDelivery.BLOCKED, state.delivery)
        assertTrue(state.feedback.contains("responsável ativo"))
        assertTrue(state.messages.isEmpty())
    }

    @Test fun invalidPhoneBlocksSending() {
        val base = initial()
        val result = engine.reduce(base.copy(tutor = base.tutor.copy(phone = "invalid")), DemoCommand.Send)
        assertEquals(DemoDelivery.BLOCKED, result.delivery)
        assertTrue(result.messages.isEmpty())
    }

    @Test fun dueDateMustBeInsideConfiguredWindow() {
        for (days in listOf(-1, 8)) {
            val base = initial()
            val changed = base.copy(procedure = base.procedure.copy(lastVisit = DemoDates.addDays(reference, days - 30)))
            val result = engine.reduce(changed, DemoCommand.Send)
            assertEquals(DemoDelivery.BLOCKED, result.delivery)
            assertEquals(0, result.attempts)
        }
    }

    @Test fun confirmationIsAttendanceAndNotClinicalCompletion() {
        val state = answer(sent(), DemoReply.CONFIRM)
        assertEquals(StatusAgendamento.AGENDADO, state.appointmentStatus)
        assertEquals(DemoAttendance.CONFIRMED, state.attendance)
        assertEquals("confirmed", state.attendance.backendValue)
        assertEquals("Confirmar", state.lastInteraction)
        assertEquals(3, state.messages.size)
        assertFalse(state.canReply)
    }

    @Test fun cancellationPreservesOccurrenceAndHistory() {
        val before = sent()
        val after = answer(before, DemoReply.CANCEL)
        assertEquals(StatusAgendamento.CANCELADO, after.appointmentStatus)
        assertEquals(DemoAttendance.CANCELLED, after.attendance)
        assertEquals(before.procedure, after.procedure)
        assertEquals(before.appointmentDate, after.appointmentDate)
        assertTrue(after.events.size > before.events.size)
        assertEquals(before.messages.first(), after.messages.first())
    }

    @Test fun rescheduleCreatesTicketWithoutChangingDate() {
        val before = sent()
        val state = answer(before, DemoReply.RESCHEDULE)
        assertEquals(StatusAgendamento.AGENDADO, state.appointmentStatus)
        assertEquals(before.appointmentDate, state.appointmentDate)
        assertEquals(DemoAttendance.RESCHEDULE, state.attendance)
        assertEquals(1, state.openTickets)
        assertEquals("Remarcação solicitada", state.tickets.single().reason)
    }

    @Test fun unknownMessagePreservesOriginalAndCreatesHumanTicket() {
        val before = sent()
        val original = "  Preciso conversar sobre meu cachorro.\n"
        val state = text(before, original)
        assertEquals(original, state.messages.last().text)
        assertEquals(original, state.tickets.single().originalMessage)
        assertEquals(before.appointmentStatus, state.appointmentStatus)
        assertEquals(before.attendance, state.attendance)
        assertEquals(1, state.openTickets)
    }

    @Test fun receptionResolutionKeepsOriginalAndObservation() {
        val received = text(sent(), "Preciso de ajuda")
        val ticket = received.tickets.single()
        val command = DemoCommand.Resolve(ticket.id, "Recepção orientou o tutor.")
        val result = engine.reduce(received, command)
        assertEquals(0, result.openTickets)
        assertEquals("Recepção orientou o tutor.", result.tickets.single().note)
        assertEquals(received.messages, result.messages)
        assertEquals(received.appointmentDate, result.appointmentDate)
        assertSame(result, engine.reduce(result, command))
    }

    @Test fun stopRevokesConsentAndPreventsNewSends() {
        val state = text(sent(DemoScenario.REVOKED), " parar ")
        assertFalse(state.tutor.consent)
        assertEquals(DemoDelivery.REVOKED, state.delivery)
        assertFalse(state.canReply)
        assertSame(state, engine.reduce(state, DemoCommand.Send))
        val checked = engine.reduce(state, DemoCommand.CheckRevocation)
        assertEquals(state.messages, checked.messages)
        assertTrue(checked.feedback.contains("Nenhuma nova mensagem"))
        assertSame(checked, engine.reduce(checked, DemoCommand.CheckRevocation))
    }

    @Test fun transientFailureSchedulesRetryWithoutFakeDeliveredMessage() {
        val state = sent(DemoScenario.FAILURE)
        assertEquals(DemoDelivery.RETRY, state.delivery)
        assertEquals(1, state.attempts)
        assertTrue(requireNotNull(state.nextRetryMinute) > state.minute)
        assertTrue(state.messages.isEmpty())
        assertNull(state.outboundId)
        assertFalse(state.events.any { it.text.startsWith("Envio simulado registrado") })
    }

    @Test fun retryAdvancesControlledClockAndCanSucceed() {
        val first = sent(DemoScenario.FAILURE)
        val state = engine.reduce(first, DemoCommand.Retry(1, true))
        assertEquals(DemoDelivery.SENT, state.delivery)
        assertEquals(2, state.attempts)
        assertTrue(state.minute >= requireNotNull(first.nextRetryMinute))
        assertEquals(1, state.messages.size)
        assertNull(state.nextRetryMinute)
    }

    @Test fun persistentFailuresHaveBoundedAttemptsAndHumanTicket() {
        val first = sent(DemoScenario.FAILURE)
        val second = engine.reduce(first, DemoCommand.Retry(1, false))
        val third = engine.reduce(second, DemoCommand.Retry(2, false))
        assertEquals(DemoDelivery.FAILED, third.delivery)
        assertEquals(DemoEngine.MAX_ATTEMPTS, third.attempts)
        assertEquals(1, third.openTickets)
        assertTrue(third.messages.isEmpty())
        assertSame(third, engine.reduce(third, DemoCommand.Retry(3, false)))
    }

    @Test fun sendIsIdempotentPerOccurrence() {
        val once = sent()
        assertSame(once, engine.reduce(once, DemoCommand.Send))
        assertEquals(1, once.messages.size)
    }

    @Test fun buttonResponsesAreIdempotentAndRejectForeignContext() {
        val before = sent()
        assertSame(before, engine.reduce(before, DemoCommand.Reply("another-message", DemoReply.CONFIRM)))
        val command = DemoCommand.Reply(requireNotNull(before.outboundId), DemoReply.CONFIRM)
        val after = engine.reduce(before, command)
        assertSame(after, engine.reduce(after, command))
        assertSame(after, answer(after, DemoReply.CANCEL))
    }

    @Test fun cancelledAppointmentGetsClinicalMessageWithoutAttendanceActions() {
        val state = sent(DemoScenario.CANCELLED)
        assertEquals(DemoDelivery.SENT, state.delivery)
        assertEquals(StatusAgendamento.CANCELADO, state.appointmentStatus)
        assertFalse(state.consultationMessage)
        assertFalse(state.canReply)
        assertFalse(state.messages.single().text.contains("escolha Confirmar"))
        assertEquals(initial(DemoScenario.CANCELLED).procedure.dueDate, state.procedure.dueDate)
        assertSame(state, answer(state, DemoReply.CONFIRM))
        assertEquals(1, text(state, "Preciso remarcar").openTickets)
    }

    @Test fun replayOfOldRetryCannotAdvanceAnotherAttempt() {
        val first = sent(DemoScenario.FAILURE)
        val retry = DemoCommand.Retry(1, false)
        val second = engine.reduce(first, retry)
        assertSame(second, engine.reduce(second, retry))
        assertEquals(2, second.attempts)
    }

    @Test fun repeatedInputEventDoesNotDuplicateConversationOrTickets() {
        val before = sent()
        val input = DemoCommand.Text(requireNotNull(before.outboundId), 0, "Outra mensagem")
        val once = engine.reduce(before, input)
        assertSame(once, engine.reduce(once, input))
        assertEquals(1, once.tickets.size)
    }

    @Test fun blankInputOrResolutionDoesNotFabricateEvents() {
        val state = sent()
        assertSame(state, text(state, "  "))
        val withTicket = text(state, "Ajuda")
        assertSame(withTicket, engine.reduce(withTicket, DemoCommand.Resolve(1, " ")))
    }

    @Test fun historyHasMonotonicClockAndChronologicalOrder() {
        val state = answer(sent(), DemoReply.CONFIRM)
        assertEquals((1..state.events.size).toList(), state.events.map { it.order })
        assertTrue(state.events.zipWithNext().all { (a, b) -> a.minute < b.minute })
        assertTrue(state.messages.all { it.minute <= state.minute })
    }

    @Test fun resetRestoresAllFictitiousDataAndScenarioIndependence() {
        val model = WhatsAppDemoViewModel()
        model.selectScenario(DemoScenario.REVOKED)
        model.dispatch(DemoCommand.Send)
        val state = model.state
        model.dispatch(DemoCommand.Text(requireNotNull(state.outboundId), 0, "PARAR"))
        model.selectTab(DemoTab.HISTORY)
        model.reset()
        assertEquals(DemoFixtures.initial(DemoScenario.REVOKED, model.state.referenceDate), model.state)
        assertEquals(DemoTab.CHAT, model.tab)
        model.selectScenario(DemoScenario.CANCELLED)
        assertEquals(StatusAgendamento.CANCELADO, model.state.appointmentStatus)
        model.selectScenario(DemoScenario.NORMAL)
        assertEquals(StatusAgendamento.AGENDADO, model.state.appointmentStatus)
        assertTrue(model.state.messages.isEmpty())
        assertTrue(model.state.tickets.isEmpty())
    }

    @Test fun newSessionsDoNotShareMutableDemoState() {
        val first = WhatsAppDemoViewModel()
        first.dispatch(DemoCommand.Send)
        val second = WhatsAppDemoViewModel()
        assertTrue(second.state.messages.isEmpty())
        assertEquals(DemoDelivery.READY, second.state.delivery)
        assertEquals(DemoDelivery.SENT, first.state.delivery)
    }

    @Test fun guidedStepsAreOfflineAndDeterministic() {
        val model = WhatsAppDemoViewModel()
        model.selectScenario(DemoScenario.FAILURE)
        model.nextStep()
        assertEquals(DemoDelivery.RETRY, model.state.delivery)
        model.nextStep()
        assertEquals(DemoDelivery.SENT, model.state.delivery)
        model.nextStep()
        assertEquals(DemoAttendance.CONFIRMED, model.state.attendance)
        model.nextStep()
        assertEquals(DemoTab.AGENDA, model.tab)
    }

    @Test fun demoDoesNotOpenRoomAtAnyPoint() {
        mockkObject(ZeloDatabase.Companion)
        try {
            every { ZeloDatabase.getInstance(any()) } throws AssertionError("Demo must never open Room")
            val model = WhatsAppDemoViewModel()
            for (scenario in DemoScenario.entries) {
                model.selectScenario(scenario)
                repeat(4) { model.nextStep() }
                model.reset()
            }
            verify(exactly = 0) { ZeloDatabase.getInstance(any()) }
        } finally { unmockkObject(ZeloDatabase.Companion) }
    }
}
