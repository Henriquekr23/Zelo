package com.example.zelo

import android.content.Intent
import android.os.SystemClock
import android.view.KeyEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

/** UI-only fictitious sessions: never fills credentials or triggers real synchronization. */
@RunWith(AndroidJUnit4::class)
class WhatsAppDemoTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @Test fun configOpensDemoWithoutCredentialsAndReturns() {
        val intent = Intent(instrumentation.targetContext, WhatsAppActivity::class.java)
        ActivityScenario.launch<WhatsAppActivity>(intent).use {
            click("Experimentar demonstração")
            awaitText("MODO DEMONSTRAÇÃO")
            awaitText("Simulação educativa. Nenhuma mensagem real será enviada.")
            click("Próximo passo")
            click("Confirmar")
            awaitText("Comparecimento confirmado. O procedimento ainda não foi concluído.")
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            awaitText("Experimentar demonstração")
        }
    }

    @Test fun repliesUpdateAgendaAndReceptionAndReset() {
        ActivityScenario.launch<WhatsAppDemoActivity>(demoIntent()).use {
            click("Próximo passo")
            click("Confirmar")
            click("Próximo passo")
            awaitText("Comparecimento: Confirmado")
            awaitText("Agendamento: AGENDADO")

            click("Reiniciar demonstração")
            click("Próximo passo")
            click("Remarcar")
            click("Próximo passo")
            awaitText("Pendência #1")
            click("Resolver pendência")
            click("Registrar resolução")
            awaitText("Resolvida")
            click("Agenda")
            awaitText("Comparecimento: Remarcação solicitada")
            awaitText("Agendamento: AGENDADO")

            click("Reiniciar demonstração")
            click("Próximo passo")
            click("Cancelar")
            click("Próximo passo")
            awaitText("Agendamento: CANCELADO")
            awaitText("Comparecimento: Cancelado")
            click("Reiniciar demonstração")
            awaitText("Comparecimento: Aguardando confirmação")
        }
    }

    @Test fun unknownTextAndRuleScenariosAreInteractive() {
        ActivityScenario.launch<WhatsAppDemoActivity>(demoIntent()).use {
            click("Próximo passo")
            click("Enviar outra mensagem")
            click("Enviar na simulação")
            click("Próximo passo")
            awaitText("Atendimento humano necessário")
            awaitText("Mensagem original: Preciso conversar com alguém sobre meu cachorro.")

            scenario("Tutor sem consentimento")
            click("Próximo passo")
            awaitText("O tutor não autorizou o recebimento de lembretes.")

            scenario("Agendamento cancelado")
            click("Próximo passo")
            awaitText("Agendamento: CANCELADO")
            click("Próximo passo")
            awaitText("Comparecimento: Cancelado")

            scenario("Falha no envio")
            click("Próximo passo")
            click("Próximo passo")
            click("Confirmar")
            click("Próximo passo")
            awaitText("Comparecimento: Confirmado")

            scenario("Revogação por PARAR")
            click("Próximo passo")
            click("Próximo passo")
            click("Próximo passo")
            awaitText("O tutor não autorizou novos lembretes. Nenhuma nova mensagem foi enviada.")
        }
    }

    private fun demoIntent() = Intent(instrumentation.targetContext, WhatsAppDemoActivity::class.java)
    private fun scenario(title: String) {
        click("Selecionar cenário")
        click(title)
    }

    private fun find(text: String): AccessibilityNodeInfo? =
        instrumentation.uiAutomation.rootInActiveWindow?.findAccessibilityNodeInfosByText(text)
            ?.firstOrNull { it.isVisibleToUser && (it.text?.toString() == text || it.contentDescription?.toString() == text) }

    private fun awaitText(text: String) {
        repeat(30) { attempt ->
            if (find(text) != null) return
            scroll(instrumentation.uiAutomation.rootInActiveWindow,
                if (attempt % 16 < 8) AccessibilityNodeInfo.ACTION_SCROLL_FORWARD else AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
            SystemClock.sleep(150)
        }
        fail("Expected demo text: $text")
    }

    private fun click(text: String) {
        repeat(30) { attempt ->
            var node = find(text)
            while (node != null && !node.isClickable) node = node.parent
            if (node?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true) {
                SystemClock.sleep(350)
                return
            }
            scroll(instrumentation.uiAutomation.rootInActiveWindow,
                if (attempt % 16 < 8) AccessibilityNodeInfo.ACTION_SCROLL_FORWARD else AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
            SystemClock.sleep(150)
        }
        fail("No demo action: $text")
    }

    private fun scroll(node: AccessibilityNodeInfo?, action: Int): Boolean {
        if (node == null) return false
        if (node.isScrollable && node.performAction(action)) return true
        for (index in 0 until node.childCount) if (scroll(node.getChild(index), action)) return true
        return false
    }
}
