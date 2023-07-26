package rules.climateControl.conditioner.ir

import models.TextDevice
import rules.RuleInfo
import rules.data.FieldData
import rules.data.MqttData

class MqttIRConditionerData(info: RuleInfo) : IRConditionerData(info), MqttData {
    override val mqttFields: Map<FieldData, TextDevice> = buildMqttFields()
    override fun complete(): Boolean {
        updateValues()
        return super.complete()
    }
}
