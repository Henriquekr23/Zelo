package com.example.zelo.data.remote

import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class SyncConflict : Exception("Há alterações no servidor. Resolva o conflito antes de reenviar.")

/** Staff bearer token belongs to this session only. Meta credentials never reach Android. */
class ZeloApi(baseUrl: String, private val token: String) {
    private val base = baseUrl.trimEnd('/')

    init {
        val url = URL(base)
        require(url.protocol == "https" && url.userInfo == null && url.query == null) {
            "Informe uma URL HTTPS sem credenciais."
        }
        require(token.length >= 32) { "Informe a credencial fornecida pela clínica." }
    }

    fun request(path: String, method: String = "GET", body: JSONObject? = null): String {
        val connection = URL(base + path).openConnection() as HttpsURLConnection
        try {
            connection.requestMethod = method
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 10000
            connection.readTimeout = 20000
            connection.setRequestProperty("Authorization", "Bearer $token")
            if (body != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            }
            val code = connection.responseCode
            if (code == 409) throw SyncConflict()
            check(code in 200..299) { "Servidor respondeu HTTP $code. Nenhuma pendência foi descartada." }
            return connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    fun records() = JSONArray(request("/v1/records"))
    fun clinic() = JSONObject(request("/v1/clinic"))
    fun communications() = JSONObject(request("/v1/communications"))
    fun save(body: JSONObject) = JSONObject(request("/v1/records", "PUT", body))
}
