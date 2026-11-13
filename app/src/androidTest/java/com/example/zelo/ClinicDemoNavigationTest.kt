package com.example.zelo

import android.content.Intent
import android.view.KeyEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Read-only profile navigation and in-memory clinic route; no location or network requests. */
@RunWith(AndroidJUnit4::class)
class ClinicDemoNavigationTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val ui = DemoUiDriver(instrumentation)

    @Test fun profileOpensClinicAndSimplifiedWhatsAppAndReturns() {
        val intent = Intent(instrumentation.targetContext, MainActivity::class.java).putExtra("TELA", "perfil")
        ActivityScenario.launch<MainActivity>(intent).use {
            click("Minha clínica")
            awaitText("CLÍNICA DEMONSTRATIVA")
            awaitText("Mapa ilustrativo — localização fictícia.")
            click("Simular trajeto")
            awaitText("Trajeto demonstrativo")
            awaitText("1,2 km • 4 min — valores ilustrativos")
            click("Ocultar trajeto")
            awaitText("Simular trajeto")
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            click("Lembretes pelo WhatsApp")
            awaitText("Experimentar demonstração")
            assertFalse(hasTechnicalInput(instrumentation.uiAutomation.rootInActiveWindow))
            assertNull(ui.findText("Sincronizar com a clínica"))
            click("Experimentar demonstração")
            awaitText("MODO DEMONSTRAÇÃO")
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            awaitText("Experimentar demonstração")
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            awaitText("Lembretes pelo WhatsApp")
        }
    }

    @Test fun clinicRouteSurvivesRotationButNewSessionStartsWithoutRoute() {
        val intent = Intent(instrumentation.targetContext, ClinicDemoActivity::class.java)
        ActivityScenario.launch<ClinicDemoActivity>(intent).use { scenario ->
            click("Simular trajeto")
            awaitText("Trajeto demonstrativo")
            scenario.recreate()
            awaitText("Ocultar trajeto")
            click("Ocultar trajeto")
            awaitText("Simular trajeto")
        }
        ActivityScenario.launch<ClinicDemoActivity>(intent).use {
            awaitText("Simular trajeto")
        }
    }

    private fun hasTechnicalInput(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false
        if (node.isEditable || node.className?.toString() == "android.widget.Switch") return true
        return (0 until node.childCount).any { hasTechnicalInput(node.getChild(it)) }
    }

    private fun awaitText(text: String) = ui.awaitText(text)
    private fun click(text: String) = ui.click(text)

}
