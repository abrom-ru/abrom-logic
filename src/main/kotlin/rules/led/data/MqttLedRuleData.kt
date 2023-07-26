package rules.led.data

import models.TextDevice
import rules.Rule
import rules.RuleInfo
import rules.data.FieldData
import rules.data.MqttData

class MqttLedRuleData(info: RuleInfo) : LedRuleData(info), MqttData {
    override val mqttFields: Map<FieldData, TextDevice> = buildMqttFields()

    override fun toRule(): Rule? {
        updateValues()
        return super.toRule()
    }
}
