package com.example.zelo

import android.content.Intent
import android.view.KeyEvent
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
    private val ui = DemoUiDriver(instrumentation)
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
            click("Lembretes pelo WhatsApp")
            awaitText("Lembretes pelo WhatsApp")
            awaitText("Experimentar demonstração")
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            awaitText("Lembretes pelo WhatsApp")
            click("Início")
            awaitText("Ações rápidas")
        }
    }

    private fun awaitText(text: String) = ui.awaitText(text)
    private fun click(text: String) = ui.click(text)

}
