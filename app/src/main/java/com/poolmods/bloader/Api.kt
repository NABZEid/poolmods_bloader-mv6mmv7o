package com.poolmods.bloader

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Klien jembatan lokal Villiard (127.0.0.1:38417). Hanya untuk game Villiard. */
object Api {
    private const val BASE = "http://127.0.0.1:38417"
    private const val TOKEN = "BLDR-7f3a91c2-VLRD-ONLY"

    private fun call(path: String, body: String? = null): JSONObject? = try {
        val c = URL(BASE + path).openConnection() as HttpURLConnection
        c.connectTimeout = 1500
        c.readTimeout = 1500
        c.setRequestProperty("x-bloader-token", TOKEN)
        if (body != null) {
            c.requestMethod = "POST"
            c.doOutput = true
            c.setRequestProperty("Content-Type", "application/json")
            c.outputStream.use { it.write(body.toByteArray()) }
        }
        if (c.responseCode == 200) JSONObject(c.inputStream.bufferedReader().readText()) else null
    } catch (e: Exception) { null }

    fun state() = call("/state")
    fun set(key: String, v: Boolean) = call("/set", JSONObject().put(key, v).toString())
    fun inject(times: Int): JSONObject? { var r: JSONObject? = null; repeat(times) { r = call("/coins") }; return r }
}
