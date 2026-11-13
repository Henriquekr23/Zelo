package com.example.zelo.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.zelo.demo.whatsapp.*

enum class DemoTab(val title: String) {
    CHAT("Conversa"), AGENDA("Agenda"), RECEPTION("Recepção"), HISTORY("Histórico")
}

/** A session exists only in this ViewModel. No Application, persistence or network dependency. */
class WhatsAppDemoViewModel : ViewModel() {
    private val referenceDate = DemoDates.today()
    private val engine = DemoEngine()
    var state by mutableStateOf(DemoFixtures.initial(DemoScenario.NORMAL, referenceDate))
        private set
    var tab by mutableStateOf(DemoTab.CHAT)
        private set

    fun dispatch(command: DemoCommand) { state = engine.reduce(state, command) }
    fun selectTab(value: DemoTab) { tab = value }
    fun selectScenario(value: DemoScenario) {
        state = DemoFixtures.initial(value, referenceDate)
        tab = DemoTab.CHAT
    }
    fun reset() = selectScenario(state.scenario)

    fun nextStep() {
        val current = state
        when {
            current.delivery == DemoDelivery.READY -> dispatch(DemoCommand.Send)
            current.delivery == DemoDelivery.RETRY -> dispatch(DemoCommand.Retry(current.attempts, true))
            current.scenario == DemoScenario.REVOKED && current.delivery == DemoDelivery.SENT ->
                dispatch(DemoCommand.Text(requireNotNull(current.outboundId), current.inputSequence, "PARAR"))
            current.delivery == DemoDelivery.REVOKED && !current.revocationChecked -> dispatch(DemoCommand.CheckRevocation)
            current.openTickets > 0 -> tab = DemoTab.RECEPTION
            current.canReply -> dispatch(DemoCommand.Reply(requireNotNull(current.outboundId), DemoReply.CONFIRM))
            current.delivery == DemoDelivery.BLOCKED || current.delivery == DemoDelivery.REVOKED -> tab = DemoTab.HISTORY
            else -> tab = DemoTab.AGENDA
        }
    }
}
