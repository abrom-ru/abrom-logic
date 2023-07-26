package rules.climateControl.conditioner.smart

import models.TextDevice
import rules.RuleInfo
import rules.data.FieldData
import rules.data.MqttData

class MqttSmartConditionerData(info: RuleInfo) : SmartConditionerData(info), MqttData {
    override val mqttFields: Map<FieldData, TextDevice> = buildMqttFields()
    override fun complete(): Boolean {
        updateValues()
        return super.complete()
    }
}
