package net.vanolex


import io.ktor.server.cio.*
import java.net.MalformedURLException
import java.net.URL
import java.sql.Connection
import kotlin.math.max

val requestMap: MutableMap<String, Long> = mutableMapOf()
val qrMap: MutableMap<String, ByteArray> = mutableMapOf()

lateinit var config: Config
lateinit var mysqlConnection: Connection
lateinit var server: CIOApplicationEngine

fun isValidUrl(url: String): Boolean {
    return try {
        URL(url)
        true
    } catch (e: MalformedURLException) {
        false
    }
}

fun isRequestAllowed(ip: String): Boolean {
    val currentTime = System.currentTimeMillis()
    val blockUntil = requestMap.getOrDefault(ip, 0L)
    val verdict = currentTime >= blockUntil
    requestMap[ip] = max(blockUntil, currentTime + if (verdict) 1500L else 3000L)
    return verdict
}

fun updateBlock(ip: String, x: Int) {
    val currentTime = System.currentTimeMillis()
    requestMap[ip] = currentTime + x * 1000L
}

fun isValidCode(str: String): Boolean {
    if (str.length != 4) return false
    for (i in str) if (i !in b64Charset) return false
    return true
}
