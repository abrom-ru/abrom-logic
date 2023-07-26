package storage

import LaunchParams
import Logger
import io.ktor.util.logging.error
import models.Topic
import models.TopicType
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttMessage
import storage.mqttModels.Mqtt
import storage.mqttModels.MqttClientHelper
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Topic base класс для общение с mqtt
 *
 */

object TopicBase {
    private val lock = ReentrantLock()
    private var mqttClientHelper: Mqtt = MqttClientHelper()
    private val base: MutableMap<Topic, String> = mutableMapOf()
    private val topicType: MutableMap<Topic, TopicType> = mutableMapOf()
    private var unsubscribedTopic = mutableListOf<Topic>()
    private val topicCallback: MutableMap<Topic, MutableList<() -> Unit>> = mutableMapOf()
    private var connectState = false
    private var connectTime: Long = 0

    /**
     * Get получить значение топика
     *
     * @param topic топик значение которого хотим получить
     * @return значение топика в формате String или null
     */
    fun get(topic: Topic): String? = lock.withLock {
        return base[topic]
    }

    /**
     * Set задать значения топика
     *
     * @param topic топик значение которого мы хотим задать
     * @param value значение
     */

    fun set(topic: Topic, value: Any) = lock.withLock {
        if (mqttClientHelper.isConnected()) {
            val needToSend = base[topic] != value
            base[topic] = value.toString()
            if (needToSend) {
                mqttClientHelper.publish(topic, base[topic] ?: "")
            }
        }
    }

    /**
     * Set topic type задать значение топика(нужна только если это кнопка)
     *
     */

    fun setTopicType(topic: Topic, type: TopicType) = lock.withLock {
        if (topic.isEmpty()) return
        if (mqttClientHelper.isConnected()) {
            mqttClientHelper.subscribe(topic)
        } else {
            unsubscribedTopic.add(topic)
        }
        topicType[topic] = type
    }

    /**
     * Update value обновить значение топика
     *
     * @param topic
     * @param text новое значение
     */
    private fun updateValue(topic: Topic, text: String) = lock.withLock {
        if (topicType[topic] == TopicType.BUTTON && !timer.isCorrect()) return
        if (topicType.containsKey(topic)) {
            Logger.debug {
                "update value of $topic to $text"
            }
            base[topic] = text
        }
    }

    /**
     * Сбросить значения кнопок
     *
     */
    private fun clearButtonState() = lock.withLock {
        base.forEach {
            if (topicType[it.key] == TopicType.BUTTON) {
                base[it.key] = "0"
            }
        }
    }

    /**
     * Subscribe to topics подписаться на топики на которые ещё не подписались
     *
     */

    private fun subscribeToTopics() {
        unsubscribedTopic.forEach { mqttClientHelper.subscribe(it) }
        unsubscribedTopic.clear()
    }

    /**
     * задать callback на топики
     *
     */

    private fun setCallBack() {
        mqttClientHelper.setCallBack(object : MqttCallbackExtended {
            override fun connectionLost(cause: Throwable?) {
                Logger.error {
                    "CONNECTION LOST with $cause" + (cause?.stackTraceToString() ?: "")
                }
                System.exit(1)
            }

            override fun messageArrived(topicsStr: String, message: MqttMessage?) {
                val topic = Topic(topicsStr)
                if (!topic.isEmpty()) {
                    val text = message?.toString() ?: ""
                    updateValue(topic, text)
                }
                Logger.debug {
                    "mqtt get message \"$message\" from topic \"$topic\""
                }
                checkRules(topic)
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
            }

            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                Logger.info {
                    "Connected to mqtt"
                }
                subscribeToTopics()
            }
        })
    }

    /**
     * Добавить callback на топики
     *
     * @param topics топики на которые хотим добавить callback
     * @param callback callback который хотим добавить
     */
    fun addCallback(topics: Iterable<Topic>, callback: () -> Unit) {
        topics.forEach {
            if (!(it.isEmpty() || it == Topic.empty)) {
                topicCallback[it]?.add(callback) ?: topicCallback.put(it, mutableListOf(callback))
            }
        }
    }

    /**
     * Check rules вызвать все callback'и topic'а
     *
     * @param topic
     */

    private fun checkRules(topic: Topic) = lock.withLock {
        try {
            if (isInitialized()) {
                topicCallback[topic]?.forEach {
                    it.invoke()
                }
                clearButtonState()
            }
        } catch (ex: Exception) {
            Logger.error {
                ex.toString()
            }
        }
    }

    /**
     * Подключиться к mqtt
     *
     */
    fun connect() {
        setCallBack()
        mqttClientHelper.connect()
        connectTime = System.currentTimeMillis()
    }

    /**
     * Инициализировать все правила
     *
     */
    private fun initializeRules() {
        try {
            topicCallback.values.flatten().forEach {
                it.invoke()
            }
        } catch (ex: Exception) {
            Logger.error(ex)
        }
    }

    /**
     * Проверить инициализацию правил
     *
     * @return инициализировано ли правило
     */
    private fun isInitialized(): Boolean {
        if (!connectState) {
            connectState =
                (System.currentTimeMillis() - connectTime > initializationDelay)
            if (connectState) {
                initializeRules()
            }
        }
        return connectState
    }

    /**
     * Удалить callback для все топиков
     *
     * @param callback
     */
    fun removeCallback(callback: () -> Unit) {
        topicCallback.forEach { callbacks ->
            callbacks.value.removeAll {
                it == callback
            }
        }
    }

    val timer = UpdateTimer()
    private const val initializationDelay = LaunchParams.INITIALIZATION_DELAY
}

class UpdateTimer {
    private var lastUpdate: Long

    init {
        lastUpdate = System.currentTimeMillis()
    }

    fun updateLast() {
        lastUpdate = System.currentTimeMillis()
    }

    fun isCorrect(): Boolean {
        return System.currentTimeMillis() - lastUpdate >= delay
    }

    companion object {
        const val delay = 5000L
    }
}
