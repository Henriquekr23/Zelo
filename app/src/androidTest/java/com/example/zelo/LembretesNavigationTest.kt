package com.example.zelo

import android.content.Intent
import android.os.SystemClock
import android.view.KeyEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Read-only navigation checks: no clinic credentials, record edits or WhatsApp requests. */
@RunWith(AndroidJUnit4::class)
class LembretesNavigationTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val demo = "Dados demonstrativos — não representam mensagens enviadas pelo WhatsApp."

    @Test fun lembretesMantemTelaOriginalEConfiguracaoPermaneceNoPerfil() {
        val intent = Intent(instrumentation.targetContext, MainActivity::class.java)
            .putExtra("TELA", "lembretes")
        ActivityScenario.launch<MainActivity>(intent).use {
            awaitText(demo)
            click("Voltar ao início")
            awaitText("Ações rápidas")
            click("Ver lembretes")
            awaitText(demo)
            click("Perfil")
            click("🔔 Configurar lembretes pelo WhatsApp")
            awaitText("Lembretes pelo WhatsApp")
            awaitText("Sincronizar com a clínica")
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            awaitText("🔔 Configurar lembretes pelo WhatsApp")
            click("Início")
            awaitText("Ações rápidas")
        }
    }

    private fun awaitText(text: String) {
        val deadline = SystemClock.uptimeMillis() + 10000
        while (SystemClock.uptimeMillis() < deadline) {
            if (find(text) != null) return
            scroll(instrumentation.uiAutomation.rootInActiveWindow)
            SystemClock.sleep(100)
        }
        fail("Expected navigation text: $text")
    }

    private fun click(text: String) {
        repeat(20) {
            var node = find(text)
            if (node != null) {
                while (node != null && !node.isClickable) node = node.parent
                if (node?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true) {
                    SystemClock.sleep(300)
                    return
                }
            }
            scroll(instrumentation.uiAutomation.rootInActiveWindow)
            SystemClock.sleep(200)
        }
        fail("No clickable navigation target: $text")
    }

    private fun find(text: String): AccessibilityNodeInfo? =
        instrumentation.uiAutomation.rootInActiveWindow?.findAccessibilityNodeInfosByText(text)
            ?.firstOrNull { it.isVisibleToUser && (it.text?.toString() == text || it.contentDescription?.toString() == text) }

    private fun scroll(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false
        if (node.isScrollable && node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)) return true
        for (index in 0 until node.childCount) if (scroll(node.getChild(index))) return true
        return false
    }
}
