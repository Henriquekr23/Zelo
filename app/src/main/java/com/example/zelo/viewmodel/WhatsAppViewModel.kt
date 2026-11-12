package com.example.zelo.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zelo.data.remote.ZeloApi
import com.example.zelo.data.repository.TutorRepository
import com.example.zelo.data.repository.WhatsAppRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.json.JSONObject

class WhatsAppViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = WhatsAppRepository(application)
    var name by mutableStateOf(TutorRepository(application).nome.value)
    var phone by mutableStateOf("")
    var consent by mutableStateOf(false)
    var enabled by mutableStateOf(false)
    var evidence by mutableStateOf("")
    var url by mutableStateOf("")
    // Per-user clinic credential, held only in memory for this screen session.
    var token by mutableStateOf("")
    var busy by mutableStateOf(false)
        private set
    var status by mutableStateOf("Conecte uma clínica para consultar os lembretes.")
        private set
    var connected by mutableStateOf(false)
        private set
    var provider by mutableStateOf("disabled")
        private set
    var clinicEnabled by mutableStateOf(false)
        private set
    var history by mutableStateOf<JSONObject?>(null)
        private set

    init { perform { restoreDraft() } }

    private suspend fun restoreDraft() {
        val draft = repository.draft()
        name = draft.optString("name", name)
        phone = draft.optString("phone")
        consent = draft.optBoolean("consent")
        enabled = draft.optBoolean("enabled")
        evidence = draft.optString("evidence")
    }

    fun saveLocal() = perform {
        repository.saveDraft(name, phone.trim(), consent, enabled, evidence.trim())
        status = "Preferências salvas no aparelho. Sincronize para aplicá-las na clínica."
    }

    fun sync(useServer: Boolean = false) = perform {
        repository.saveDraft(name, phone.trim(), consent, enabled, evidence.trim())
        val result = repository.synchronize(ZeloApi(url.trim(), token), useServer)
        val clinic = result.getJSONObject("clinic")
        connected = true
        provider = clinic.getString("provider")
        clinicEnabled = clinic.getBoolean("enabled")
        history = result.getJSONObject("history")
        restoreDraft()
        status = if (provider == "mock") "Sincronizado em modo de simulação; nenhuma mensagem real enviada."
        else if (!clinicEnabled) "Sincronizado. A clínica ainda não ativou os envios."
        else "Sincronizado. Consulte abaixo o estado dos lembretes."
    }

    private fun perform(block: suspend () -> Unit) {
        if (busy) return
        busy = true
        viewModelScope.launch {
            try { block() }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (failure: Exception) {
                connected = false
                status = failure.message ?: "Não foi possível conectar. As pendências foram preservadas."
            } finally { busy = false }
        }
    }
}
