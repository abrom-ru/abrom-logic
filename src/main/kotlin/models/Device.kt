package models

import storage.TopicBase
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
abstract class Device(val topic: Topic) {

    abstract val value: Any

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other !is Device -> false
            topic != other.topic -> false
            else -> true
        }
    }

    override fun hashCode(): Int {
        return topic.hashCode()
    }

    abstract fun parse(strValue: String?): Any
    open fun deParse(value: Any): String = value.toString()
}

class SensorDevice(topic: Topic) : Device(topic) {
    constructor(deviceName: String, cellName: String) : this(Topic(deviceName, cellName))
    constructor(topic: String) : this(topic.toTopic())

    init {
        TopicBase.setTopicType(topic, TopicType.SENSOR_DEVICE)
    }

    override var value: Double
        get() = parse(TopicBase.get(topic))
        set(value) {
            TopicBase.set(topic, deParse(value))
        }

    override fun parse(strValue: String?): Double {
        return strValue?.toDoubleOrNull() ?: throw IllegalStateException("value not initialized")
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: Double) {
        value = newValue
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Double {
        return value as? Double ?: throw IllegalStateException("value not initialized")
    }
}

class SwitchDevice(topic: Topic, private val inverted: Boolean = false) : Device(topic) {
    constructor(deviceName: String, cellName: String, inverted: Boolean = false) : this(
        Topic(deviceName, cellName),
        inverted,
    )

    constructor(topic: String) : this(topic.toTopic())
    constructor(topic: Topic) : this(topic, false)

    init {
        TopicBase.setTopicType(topic, TopicType.SWITCH)
    }

    override fun parse(strValue: String?): Boolean {
        if (strValue == null) throw IllegalStateException("value not initialized")
        return if (!inverted) strValue == "1" else strValue != "1"
    }

    override var value: Boolean
        get() = parse(TopicBase.get(topic))
        set(value) {
            TopicBase.set(topic, deParse(value))
        }

    override fun deParse(value: Any): String {
        return if (value as? Boolean == true) {
            if (inverted) {
                "0"
            } else {
                "1"
            }
        } else {
            if (inverted) {
                "1"
            } else {
                "0"
            }
        }
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: Boolean) {
        value = newValue
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
        return value as? Boolean ?: throw IllegalStateException("value not initialized")
    }

    override fun toString(): String {
        return "SwitchDevice(topic = $topic; value = $value)"
    }
}

class ButtonDevice(topic: Topic, private val inverted: Boolean = false) : Device(topic) {
    constructor(deviceName: String, cellName: String, inverted: Boolean) : this(Topic(deviceName, cellName), inverted)

    constructor(topic: String) : this(topic.toTopic())
    constructor(topic: Topic) : this(topic, false)

    init {
        TopicBase.setTopicType(topic, TopicType.BUTTON)
    }

    override var value: Boolean
        get() = parse(TopicBase.get(topic))
        set(value) {
            TopicBase.set(topic, deParse(value))
        }
    val defaultValue: Boolean = false
    override fun parse(strValue: String?): Boolean {
        return strValue == "1"
    }

    override fun deParse(value: Any): String {
        return if (value as? Boolean ?: throw IllegalStateException("value not initialized")) "1" else "0"
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: Boolean) {
        value = if (inverted) newValue else !newValue
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
        val res = value as? Boolean ?: throw IllegalStateException("value not initialized")
        return if (inverted) !res else res
    }

    override fun toString(): String {
        return "ButtonDevice(topic = $topic; value = $value)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ButtonDevice) return false
        if (!super.equals(other)) return false

        if (inverted != other.inverted) return false
        return defaultValue == other.defaultValue
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + inverted.hashCode()
        result = 31 * result + defaultValue.hashCode()
        return result
    }
}

class TextDevice(topic: Topic) : Device(topic) {
    constructor(deviceName: String, cellName: String) : this(Topic(deviceName, cellName))
    constructor(topic: String) : this(topic.toTopic())

    init {
        TopicBase.setTopicType(topic, TopicType.TEXT)
    }

    override var value: String
        get() = parse(TopicBase.get(topic))
        set(value) {
            TopicBase.set(topic, deParse(value))
        }

    override fun parse(strValue: String?): String {
        return strValue ?: throw IllegalStateException("value not initialized")
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: String) {
        value = newValue
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return value as? String ?: throw IllegalStateException("value not initialized")
    }

    override fun toString(): String {
        return "TextDevice(topic = $topic; value = $value)"
    }
}

class RangeDevice(topic: Topic) : Device(topic) {
    constructor(deviceName: String, cellName: String) : this(Topic(deviceName, cellName))
    constructor(topic: String) : this(topic.toTopic())

    init {
        TopicBase.setTopicType(topic, TopicType.RANGE_DEVICE)
    }

    override fun parse(strValue: String?): Int {
        return strValue?.toIntOrNull() ?: throw IllegalStateException("value not initialized")
    }

    override var value: Int
        get() = parse(TopicBase.get(topic))
        set(value) {
            TopicBase.set(topic, deParse(value))
        }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: Int) {
        value = newValue
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return value as? Int ?: throw IllegalStateException("value not initialized")
    }

    override fun toString(): String {
        return "RangeDevice(topic = $topic; value = $value)"
    }
}

enum class TopicType {
    SWITCH, BUTTON, TEXT, SENSOR_DEVICE, RANGE_DEVICE
}
