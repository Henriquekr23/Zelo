package com.example.zelo.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.example.zelo.data.local.ZeloDatabase
import com.example.zelo.data.remote.SyncJournal
import com.example.zelo.data.remote.ZeloApi
import com.example.zelo.data.remote.canonical
import com.example.zelo.model.Agendamento
import com.example.zelo.model.StatusAgendamento
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

class WhatsAppRepository(context: Context) {
    private val database = ZeloDatabase.getInstance(context.applicationContext)
    private val agenda = AgendamentoRepository(database.agendamentoDao())
    private val journal = SyncJournal(context.applicationContext)
    companion object { private val mutex = Mutex() }

    suspend fun draft(): JSONObject = withContext(Dispatchers.IO) {
        mutex.withLock { journal.read().optJSONObject("draft") ?: JSONObject() }
    }

    private fun id(state: JSONObject, kind: String, localId: Int): String =
        UUID.nameUUIDFromBytes("${state.getString("installation")}:$kind:$localId".toByteArray(Charsets.UTF_8)).toString()

    private fun iso(value: String): String {
        val parser = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).apply { isLenient = false }
        require(Regex("[0-9]{2}/[0-9]{2}/[0-9]{4}").matches(value))
        return SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(requireNotNull(parser.parse(value)))
    }

    private fun localDate(value: String): String {
        val parts = value.split("-")
        require(parts.size == 3)
        return "${parts[2]}/${parts[1]}/${parts[0]}"
    }

    private fun appointmentBody(state: JSONObject, appointment: Agendamento, attendance: String): JSONObject =
        JSONObject().put("pet_id", id(state, "pet", appointment.petId))
            .put("tutor_id", id(state, "tutor", 1)).put("date", iso(appointment.data))
            .put("time", appointment.horario).put("description", appointment.descricao)
            .put("status", appointment.status.name).put("attendance", attendance)

    private fun queue(state: JSONObject, kind: String, localId: Int, body: JSONObject) {
        val records = state.getJSONObject("records")
        val key = id(state, kind, localId)
        val row = records.optJSONObject(key) ?: JSONObject().put("kind", kind).put("localId", localId)
            .put("version", 0).put("acceptedLocal", "")
        row.put("body", body).put("pending", canonical(body) != row.optString("acceptedLocal"))
        records.put(key, row)
    }

    private suspend fun prepare(state: JSONObject) {
        if (!state.has("clinic")) return
        val draft = state.getJSONObject("draft")
        queue(state, "tutor", 1, JSONObject().put("name", draft.getString("name"))
            .put("phone", draft.getString("phone")).put("active", true)
            .put("consent", draft.getBoolean("consent"))
            .put("reminders_enabled", draft.getBoolean("enabled"))
            .put("consent_source", draft.getString("evidence")))
        val pets = database.petDao().observarPorTutor(1).first()
        for (pet in pets) queue(state, "pet", pet.id, JSONObject()
            .put("name", pet.nome).put("tutor_id", id(state, "tutor", 1)))
        val petIds = pets.map { it.id }.toSet()
        for (appointment in agenda.todos.first().filter { it.petId in petIds }) {
            val row = state.getJSONObject("records").optJSONObject(id(state, "appointment", appointment.id))
            val attendance = row?.optJSONObject("serverData")?.optString("attendance", "pending") ?: "pending"
            queue(state, "appointment", appointment.id, appointmentBody(state, appointment, attendance))
        }
    }

    suspend fun saveDraft(name: String, phone: String, consent: Boolean, enabled: Boolean, evidence: String) =
        withContext(Dispatchers.IO) {
            mutex.withLock {
                require(name.isNotBlank()) { "Informe o nome do tutor." }
                require(phone.isEmpty() || Regex("^\\+[1-9][0-9]{7,14}$").matches(phone)) { "Use telefone com código do país: +5511999999999" }
                require(!consent || (phone.isNotEmpty() && evidence.isNotBlank())) { "Registre a origem da autorização." }
                val state = journal.read()
                state.put("draft", JSONObject().put("name", name.trim()).put("phone", phone)
                    .put("consent", consent).put("enabled", enabled).put("evidence", evidence))
                // Preserve the draft even if a legacy appointment has an invalid date.
                journal.write(state)
                prepare(state)
                journal.write(state)
            }
        }

    suspend fun synchronize(api: ZeloApi, useServer: Boolean = false): JSONObject = withContext(Dispatchers.IO) {
        mutex.withLock {
            val state = journal.read()
            val clinic = api.clinic()
            require(!state.has("clinic") || state.getString("clinic") == clinic.getString("id")) {
                "Este aparelho está vinculado a outra clínica."
            }
            state.put("clinic", clinic.getString("id"))
            journal.write(state)
            if (!useServer) {
                prepare(state)
                journal.write(state)
                val records = state.getJSONObject("records")
                val order = listOf("tutor", "pet", "appointment")
                val keys = records.keys().asSequence().toList().sortedBy { order.indexOf(records.getJSONObject(it).getString("kind")) }
                for (key in keys) {
                    val row = records.getJSONObject(key)
                    if (!row.getBoolean("pending")) continue
                    val body = row.getJSONObject("body")
                    val accepted = api.save(JSONObject().put("id", key).put("kind", row.getString("kind"))
                        .put("base_version", row.getInt("version")).put("data", body))
                    row.put("version", accepted.getInt("version")).put("acceptedLocal", canonical(body))
                        .put("serverData", accepted.getJSONObject("data")).put("pending", false)
                    journal.write(state)
                }
            }
            val remote = api.records()
            val records = state.getJSONObject("records")
            database.withTransaction {
                for (i in 0 until remote.length()) {
                    val record = remote.getJSONObject(i)
                    val key = record.getString("id")
                    val row = records.optJSONObject(key) ?: continue
                    val data = record.getJSONObject("data")
                    if (!useServer && row.optBoolean("pending")) continue
                    if (row.getString("kind") == "appointment") {
                        val local = database.agendamentoDao().buscarPorId(row.getInt("localId"))?.toDomain() ?: continue
                        val priorAttendance = row.optJSONObject("serverData")?.optString("attendance", "pending") ?: "pending"
                        check(useServer || canonical(appointmentBody(state, local, priorAttendance)) == row.optString("acceptedLocal")) {
                            "A agenda mudou durante a sincronização. Salve as pendências e tente novamente."
                        }
                        require(data.getString("pet_id") == id(state, "pet", local.petId) &&
                            data.getString("tutor_id") == id(state, "tutor", 1)) { "Vínculo alterado; procure a recepção." }
                        val updated = local.copy(data = localDate(data.getString("date")),
                            horario = data.getString("time"), descricao = data.getString("description"),
                            status = StatusAgendamento.valueOf(data.getString("status")))
                        if (updated != local) agenda.atualizar(updated)
                        row.put("acceptedLocal", canonical(appointmentBody(state, updated, data.getString("attendance"))))
                    } else {
                        if (row.getString("kind") == "pet") {
                            val pet = database.petDao().buscarPorId(row.getInt("localId")) ?: continue
                            val current = JSONObject().put("name", pet.nome).put("tutor_id", id(state, "tutor", 1))
                            check(useServer || canonical(current) == row.optString("acceptedLocal")) {
                                "O cadastro do pet mudou durante a sincronização."
                            }
                            require(data.getString("tutor_id") == id(state, "tutor", 1)) { "Tutor alterado; procure a recepção." }
                            if (pet.nome != data.getString("name")) {
                                PetRepository(database.petDao()).atualizar(pet.copy(nome = data.getString("name")))
                            }
                        }
                        row.put("acceptedLocal", canonical(data))
                        if (row.getString("kind") == "tutor") {
                            state.put("draft", JSONObject().put("name", data.getString("name"))
                                .put("phone", data.getString("phone")).put("consent", data.getBoolean("consent"))
                                .put("enabled", data.getBoolean("reminders_enabled"))
                                .put("evidence", data.getString("consent_source")))
                        }
                    }
                    row.put("version", record.getInt("version")).put("serverData", data)
                        .put("body", data).put("pending", false)
                }
            }
            // Persist accepted versions even if fetching the conversation history fails.
            journal.write(state)
            val history = api.communications()
            state.put("lastHistory", history).put("lastClinic", clinic)
            journal.write(state)
            JSONObject().put("history", history).put("clinic", clinic)
        }
    }
}
