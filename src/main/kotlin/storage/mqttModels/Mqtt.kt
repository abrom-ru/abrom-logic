package storage.mqttModels

import models.Topic
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended

interface Mqtt {
    fun subscribe(topic: String)
    fun subscribe(topic: Topic)
    fun publish(topic: String, message: String)
    fun publish(topic: Topic, msg: String)
    fun setCallBack(mqttCallbackExtended: MqttCallbackExtended)
    fun connect()
    fun isConnected(): Boolean
}
