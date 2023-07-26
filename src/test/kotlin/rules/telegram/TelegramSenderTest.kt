package rules.telegram

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class TelegramSenderTest {

    val sender = TelegramSender()

    @Test
    fun send() {
        val myKey = "98c50b34c5e757a12f5dcd4143cf127e"
        val code = sender.send(myKey, "Test is run")
        assertEquals(code, 200)
    }
}
