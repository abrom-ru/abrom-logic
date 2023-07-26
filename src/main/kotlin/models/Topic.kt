package models

/**
 * Класс топика
 *
 * @constructor Create empty Topic
 */
class Topic : Comparable<Topic> {

    /**
     * Device имя устройства
     */
    val device: String

    /**
     * канал устройства
     */
    val channel: String

    override fun toString(): String {
        return "$device/$channel"
    }

    fun longTopic(): String {
        val toString = this.toString()
        return if (toString == "") {
            ""
        } else {
            "/devices/${toString.substringBefore('/')}/controls/" + if (toString.contains('/')) {
                toString.substringAfter(
                    '/',
                )
            } else {
                ""
            }
        }
    }

    fun isEmpty(): Boolean {
        return device.isBlank() || channel.isBlank()
    }

    override fun compareTo(other: Topic): Int {
        return (device + channel).compareTo(other.device + other.channel)
    }

    override fun equals(other: Any?): Boolean {
        return when {
            other !is Topic -> false
            device != other.device -> false
            channel != other.channel -> false
            else -> true
        }
    }

    override fun hashCode(): Int {
        var result = device.hashCode()
        result = 31 * result + channel.hashCode()
        return result
    }

    constructor(topic: String) {
        device = topic.substringAfter("/devices/").substringBefore("/")
        channel = topic.substringAfterLast("/")
    }

    constructor(device: String, cell: String) {
        this.device = device.trim()
        this.channel = cell.trim()
    }

    fun copy(device: String = this.device, channel: String = this.channel): Topic {
        return Topic(device, channel)
    }

    companion object {

        val empty = Topic("", "")

        fun isTopicCorrect(topic: String): Boolean {
            return (
                topic.count { it == '/' } == 1 && topic.substringBefore('/').isNotBlank() && topic.substringAfter(
                    '/',
                ).isNotBlank()
                )
        }
    }
}

fun String?.toTopic(): Topic {
    if (this == null) return Topic.empty
    val parts = this.split("/")

    return if (parts.size == 2) Topic(parts[0].trim(), parts[1].trim()) else Topic.empty
}
