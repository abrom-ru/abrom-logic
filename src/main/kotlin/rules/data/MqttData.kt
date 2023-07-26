package rules.data

import models.TextDevice
import models.Topic

interface MqttData : RuleData {

    val mqttFields: Map<FieldData, TextDevice>
    override fun save() {
        updateValues()
        super.save()
    }

    fun updateValues() {
        values.forEach { fields[it] = mqttFields[it]?.value ?: "" }
    }

    fun buildMqttFields(): Map<FieldData, TextDevice> {
        return values.associateWith { TextDevice(getTopic(it)) }
    }

    private fun getTopic(field: FieldData): Topic {
        val jsName = "${info}_rule"
        return Topic(jsName, field.jsName)
    }
}
