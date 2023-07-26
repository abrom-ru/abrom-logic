package models

import io.mockk.every
import io.mockk.mockkObject
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import storage.TopicBase
import kotlin.test.assertEquals

class SensorDeviceTest {

    @Test
    fun getValue() {
        var currentValue: String? = "123"
        val topic = "topic/sensor".toTopic()
        every { TopicBase.get(topic) } answers { currentValue }
        val sensorDevice = SensorDevice(topic)
        assertEquals(sensorDevice.value, 123.0)
        currentValue = "321.1123"
        assertEquals(sensorDevice.value, 321.1123)
    }

    @Test
    fun getNullableValue() {
        val currentValue: String? = null
        val topic = "topic/sensor".toTopic()
        every { TopicBase.get(topic) } answers { currentValue }
        val sensorDevice = SensorDevice(topic)
        assertEquals(sensorDevice.value, 0.0)
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun mockObject() {
            mockkObject(TopicBase)
        }
    }
}
