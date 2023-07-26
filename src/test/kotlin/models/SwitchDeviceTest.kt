package models

import io.mockk.every
import io.mockk.mockkObject
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import storage.TopicBase

class SwitchDeviceTest {

    @Test
    fun getValue() {
        var currentValue: String? = "1"
        val topic = "topic/sensor".toTopic()
        every { TopicBase.get(topic) } answers { currentValue }
        val sensorDevice = SwitchDevice(topic)
        kotlin.test.assertEquals(sensorDevice.value, true)
        currentValue = "0"
        kotlin.test.assertEquals(sensorDevice.value, false)
    }

    @Test
    fun getNullableValue() {
        val currentValue: String? = null
        val topic = "topic/sensor".toTopic()
        every { TopicBase.get(topic) } answers { currentValue }
        val sensorDevice = SwitchDevice(topic)
        kotlin.test.assertEquals(sensorDevice.value, false)
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun mockObject() {
            mockkObject(TopicBase)
        }
    }
}
