package com.example.zelo.data.remote

import android.content.Context
import android.util.AtomicFile
import org.json.JSONObject
import java.io.File
import java.util.UUID

/** Separate durable journal, excluded from backup. The Room schema stays untouched. */
class SyncJournal(context: Context) {
    private val file = AtomicFile(File(context.noBackupFilesDir, "whatsapp-sync.json"))

    fun read(): JSONObject = try {
        JSONObject(file.readFully().toString(Charsets.UTF_8))
    } catch (missing: java.io.FileNotFoundException) {
        if (file.baseFile.exists() || File(file.baseFile.path + ".bak").exists()) throw missing
        JSONObject().put("installation", UUID.randomUUID().toString()).put("records", JSONObject())
    }

    fun write(data: JSONObject) {
        val stream = file.startWrite()
        try {
            stream.write(data.toString().toByteArray(Charsets.UTF_8))
            file.finishWrite(stream)
        } catch (failure: Exception) {
            file.failWrite(stream)
            throw failure
        }
    }
}

fun canonical(data: JSONObject): String = data.keys().asSequence().sorted().joinToString(",", "{", "}") {
    val value = data.opt(it)
    JSONObject.quote(it) + ":" + if (value is String) JSONObject.quote(value) else (value?.toString() ?: "null")
}
