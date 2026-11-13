package com.example.zelo

import android.content.ContextWrapper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.zelo.data.remote.SyncJournal
import com.example.zelo.data.remote.canonical
import org.json.JSONException
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class SyncJournalTest {
    private val root = File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir,
        "journal-test-" + UUID.randomUUID()).apply { mkdirs() }
    private val context = object : ContextWrapper(InstrumentationRegistry.getInstrumentation().targetContext) {
        override fun getNoBackupFilesDir(): File = root
    }

    @After fun cleanup() { root.deleteRecursively() }

    @Test fun persistsIdentityAndOfflineOperationAcrossInstances() {
        val journal = SyncJournal(context)
        val state = journal.read()
        val identity = state.getString("installation")
        state.getJSONObject("records").put("remote-id", JSONObject()
            .put("pending", true).put("version", 3).put("body", JSONObject().put("consent", false)))
        journal.write(state)
        val restored = SyncJournal(context).read()
        assertEquals(identity, restored.getString("installation"))
        assertTrue(restored.getJSONObject("records").getJSONObject("remote-id").getBoolean("pending"))
        assertFalse(restored.getJSONObject("records").getJSONObject("remote-id")
            .getJSONObject("body").getBoolean("consent"))
    }

    @Test fun fingerprintIgnoresKeyOrderButDistinguishesEscapedText() {
        val a = JSONObject().put("status", "AGENDADO").put("description", "Dose | status=CANCELADO")
        val b = JSONObject().put("description", "Dose | status=CANCELADO").put("status", "AGENDADO")
        val c = JSONObject().put("description", "Dose").put("status", "CANCELADO")
        assertEquals(canonical(a), canonical(b))
        assertNotEquals(canonical(a), canonical(c))
    }

    @Test fun corruptJournalFailsClosedInsteadOfCreatingAnotherIdentity() {
        File(root, "whatsapp-sync.json").writeText("invalid JSON")
        try {
            SyncJournal(context).read()
            fail("Corruption must not regenerate stable IDs")
        } catch (expected: JSONException) {
            assertEquals("invalid JSON", File(root, "whatsapp-sync.json").readText())
        }
    }
}
