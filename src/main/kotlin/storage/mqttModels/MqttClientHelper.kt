package storage.mqttModels

import LaunchParams
import Logger
import models.Topic
import models.toTopic
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

/**
 * MqttClientHelper class documentation.
 *
 * This class help to create mqtt connection.
 *
 * @author Abdulkhakov Artur arturabdulkhakov1@gmail.com.
 *
 * @param ip ip of broker we want to connect.
 */
class MqttClientHelper(ip: String = SOLACE_MQTT_HOST) : Mqtt {

    var mqttClient: MqttAsyncClient
    val serverUri = ip
    private val clientId: String = MqttClient.generateClientId()

    private val persistence = MemoryPersistence()

    init {
        mqttClient = MqttAsyncClient(serverUri, clientId, persistence)
    }

    /**
     * Connect to mqtt broker
     *
     */
    override fun connect() {
        val mqttConnectOptions = MqttConnectOptions().apply {
            isAutomaticReconnect = SOLACE_CONNECTION_RECONNECT
            isCleanSession = SOLACE_CONNECTION_CLEAN_SESSION
            userName = SOLACE_CLIENT_USER_NAME
            password = SOLACE_CLIENT_PASSWORD.toCharArray()
            connectionTimeout = SOLACE_CONNECTION_TIMEOUT
            keepAliveInterval = SOLACE_CONNECTION_KEEP_ALIVE_INTERVAL
        }

        try {
            mqttClient.connect(
                mqttConnectOptions,
                null,
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        val disconnectedBufferOptions =
                            DisconnectedBufferOptions().apply {
                                isBufferEnabled = true
                                bufferSize = 100
                                isPersistBuffer = false
                                isDeleteOldestMessages = false
                            }
                        mqttClient.setBufferOpts(disconnectedBufferOptions)
                    }

                    override fun onFailure(
                        asyncActionToken: IMqttToken,
                        exception: Throwable,
                    ) {
                        Logger.error {
                            "MQTT:  Failed to connect to: $serverUri ; $exception"
                        }
                    }
                },
            )
        } catch (ex: MqttException) {
            Logger.error {
                ex.stackTraceToString()
            }
        }
    }

    /**
     * Subscribe to mqtt topic
     *
     * @param topic topic we want to subscribe.
     */
    override fun subscribe(topic: String) {
        try {
            Logger.info {
                "subscribed to $topic"
            }
            mqttClient.subscribe(topic, QOS)
        } catch (ex: MqttException) {
            Logger.error {
                "Exception whilst subscribing from topic '$topic'" + ex.stackTraceToString()
            }
        }
    }

    override fun subscribe(topic: Topic) {
        subscribe(topic.longTopic())
    }

    override fun publish(topic: String, message: String) {
        this.publish(topic.toTopic(), message)
    }

    private fun unsubscribe(unsubscribeTopic: String) {
        try {
            mqttClient.unsubscribe(
                if (unsubscribeTopic == "") {
                    ""
                } else {
                    "/devices/${unsubscribeTopic.substringBefore('/')}/controls/" +
                        if (unsubscribeTopic.contains('/')) unsubscribeTopic.substringAfter('/') else ""
                },
            )
        } catch (ex: MqttException) {
            Logger.error {
                "Exception whilst unsubscribing from topic '$unsubscribeTopic'" + ex.stackTraceToString()
            }
        }
    }

    /**
     * Publish message to mqtt [topic]
     *
     * @param topic topic of message we want to send.
     * @param msg message we want to send.
     */
    override fun publish(topic: Topic, msg: String) {
        try {
            val message = MqttMessage()
            message.payload = msg.toByteArray()
            mqttClient.publish("${topic.longTopic()}/on", message.payload, QOS, false)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    /**
     * Connection state.
     *
     * @return true if client is connected and false if not
     */
    override fun isConnected(): Boolean {
        return mqttClient.isConnected
    }

    /**
     * Destroy mqtt connection.
     */
    fun destroy() {
        mqttClient.disconnect()
    }

    fun unsubscribe(unsubscribeTopic: Topic) {
        unsubscribe(unsubscribeTopic.toString())
    }

    override fun setCallBack(mqttCallbackExtended: MqttCallbackExtended) {
        mqttClient.setCallback(mqttCallbackExtended)
    }

    companion object {
        const val SOLACE_CLIENT_USER_NAME = ""
        const val SOLACE_CLIENT_PASSWORD = ""
        val SOLACE_MQTT_HOST = if (!LaunchParams.LOCAL_LAUNCH) "tcp://localhost:1883" else LaunchParams.REMOTE_IP

        // Other options
        const val SOLACE_CONNECTION_TIMEOUT = 3
        const val SOLACE_CONNECTION_KEEP_ALIVE_INTERVAL = 60
        const val SOLACE_CONNECTION_CLEAN_SESSION = true
        const val SOLACE_CONNECTION_RECONNECT = false
        const val QOS = 0
    }
}
