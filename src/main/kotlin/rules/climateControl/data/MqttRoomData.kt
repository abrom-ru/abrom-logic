package rules.climateControl.data

import models.TextDevice
import rules.RuleInfo
import rules.climateControl.Room
import rules.data.FieldData
import rules.data.MqttData

/**
 * MqttRoomData class documentation.
 *
 * This class used for parse room's parameters from json
 *
 * @author Abdulkhakov Artur arturabdulkhakov1@gmail.com
 *
 */
class MqttRoomData(info: RuleInfo) : RoomData(info), MqttData {
    override val mqttFields: Map<FieldData, TextDevice> = buildMqttFields()
    override fun toRule(): Room? {
        updateValues()
        return super.toRule()
    }
}
