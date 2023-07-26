package rules.telegram

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TelegramSender {
    fun send(key: String, message: String): Int {
        val request = HttpRequest.newBuilder().uri(URI.create(BOT_URI)).POST(getData(key, message))
            .header("Content-Type", "application/x-www-form-urlencoded").build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        // Logger.info { response.body() }
        return response.statusCode()
    }

    private fun String.utf8() = URLEncoder.encode(this, "UTF-8")

    private fun getData(key: String, message: String): HttpRequest.BodyPublisher? {
        val data = mapOf("key" to key, "message" to message)
        val res = data.map { (k, v) -> "${(k.utf8())}=${v.utf8()}" }
            .joinToString("&")
        // Logger.info { res }
        return HttpRequest.BodyPublishers.ofString(res)
    }

    companion object {
        private val client: HttpClient = HttpClient.newBuilder().build()
        private const val BOT_URI = "https://pushmebot.ru/send"
    }
}
