package rules

import rules.button.data.ButtonRuleData
import rules.button.data.DBButtonRuleData
import rules.button.data.MqttButtonRuleData
import rules.climateControl.conditioner.ir.DBIRConditionerData
import rules.climateControl.conditioner.ir.IRConditionerData
import rules.climateControl.conditioner.ir.MqttIRConditionerData
import rules.climateControl.conditioner.smart.DBSmartConditionerData
import rules.climateControl.conditioner.smart.MqttSmartConditionerData
import rules.climateControl.conditioner.smart.SmartConditionerData
import rules.climateControl.data.DBFloorControllerData
import rules.climateControl.data.FloorControllerData
import rules.climateControl.data.MqttFloorControllerData
import rules.custom.data.CustomRuleData
import rules.custom.data.DBCustomRuleData
import rules.custom.data.MqttCustomRuleData
import rules.data.FieldData
import rules.data.RuleData
import rules.led.data.DBLedRuleData
import rules.led.data.LedRuleData
import rules.led.data.MqttLedRuleData
import rules.rgb.data.DBRGDLedData
import rules.rgb.data.MqttRGBLedData
import rules.rgb.data.RGBLedData
import rules.telegram.data.DBTelegramData
import rules.telegram.data.MqttTelegramData
import rules.telegram.data.TelegramData
import rules.watering.data.DBWateringRuleData
import rules.watering.data.MqttWateringRuleData
import rules.watering.data.WateringRuleData

/**
 * Rule types
 *
 * Здесь нужно прописывать все существующие типы правил
 * Тип правила является именем enum'а
 * Для каждого правила необходимо реализовать интерфейс RuleType'а
 *
 * @property jsFields
 * @constructor Create empty Rule types
 */
enum class RuleTypes(val jsFields: JsFields) {
    CUSTOM(CustomRuleData) {
        override fun getMqttData(info: RuleInfo) = MqttCustomRuleData(info)

        override fun getDBData(info: RuleInfo) = DBCustomRuleData(info)
        override fun getDataFromHttp(info: RuleInfo, fields: Map<String, String>): RuleData =
            CustomRuleData(info).apply { fillFields(fields) }
    },
    LED(LedRuleData) {
        override fun getMqttData(info: RuleInfo) = MqttLedRuleData(info)

        override fun getDBData(info: RuleInfo) = DBLedRuleData(info)
        override fun getDataFromHttp(info: RuleInfo, fields: Map<String, String>): RuleData =
            LedRuleData(info).apply { fillFields(fields) }
    },
    BUTTON(ButtonRuleData) {
        override fun getMqttData(info: RuleInfo) = MqttButtonRuleData(info)

        override fun getDBData(info: RuleInfo) = DBButtonRuleData(info)
        override fun getDataFromHttp(info: RuleInfo, fields: Map<String, String>): RuleData =
            ButtonRuleData(info).apply { fillFields(fields) }
    },
    WATERING(WateringRuleData) {

        override fun getMqttData(info: RuleInfo) = MqttWateringRuleData(info)

        override fun getDBData(info: RuleInfo) = DBWateringRuleData(info)
        override fun getDataFromHttp(info: RuleInfo, fields: Map<String, String>): RuleData =
            WateringRuleData(info).apply { fillFields(fields) }
    },
    TELEGRAM(TelegramData) {
        override fun getMqttData(info: RuleInfo): RuleData = MqttTelegramData(info)

        override fun getDBData(info: RuleInfo): RuleData = DBTelegramData(info)
        override fun getDataFromHttp(info: RuleInfo, fields: Map<String, String>): RuleData =
            TelegramData(info).apply { fillFields(fields) }
    },
    RGB(RGBLedData) {
        override fun getMqttData(info: RuleInfo): RuleData = MqttRGBLedData(info)

        override fun getDBData(info: RuleInfo): RuleData = DBRGDLedData(info)
        override fun getDataFromHttp(info: RuleInfo, fields: Map<String, String>): RuleData =
            RGBLedData(info).apply { fillFields(fields) }
    },

    // ROOM(RoomData) {
    //     override fun getDBData(info: RuleInfo): RuleData = DBRoomData(info)
    //     override fun getDataFromHttp(info: RuleInfo, fields: Map<String, String>): RuleData =
    //         RoomData(info).ap`ply { fillFields(fields) }
    //
    //     override fun getMqttData(info: RuleInfo): RuleData = MqttRoomData(info)
    // },
    IRCONDITIONER(IRConditionerData) {
        override fun getMqttData(info: RuleInfo): RuleData = MqttIRConditionerData(info)

        override fun getDBData(info: RuleInfo): RuleData = DBIRConditionerData(info)
        override fun getDataFromHttp(info: RuleInfo, fields: Map<String, String>): RuleData =
            IRConditionerData(info).apply { fillFields(fields) }
    },
    SMART_CONDITIONER(SmartConditionerData) {
        override fun getMqttData(info: RuleInfo): RuleData = MqttSmartConditionerData(info)

        override fun getDBData(info: RuleInfo): RuleData = DBSmartConditionerData(info)
        override fun getDataFromHttp(info: RuleInfo, fields: Map<String, String>): RuleData =
            SmartConditionerData(info).apply { fillFields(fields) }
    },
    HEAT(FloorControllerData) {
        override fun getMqttData(info: RuleInfo): RuleData = MqttFloorControllerData(info)
        override fun getDBData(info: RuleInfo): RuleData = DBFloorControllerData(info)
        override fun getDataFromHttp(info: RuleInfo, fields: Map<String, String>): RuleData =
            FloorControllerData(info).apply { fillFields(fields) }
    },

    ;

    open val dbName: String = this.toString()
    abstract fun getMqttData(info: RuleInfo): RuleData
    abstract fun getDBData(info: RuleInfo): RuleData

    abstract fun getDataFromHttp(info: RuleInfo, fields: Map<String, String>): RuleData

    companion object {
        fun getRuleTypeByName(name: String): RuleTypes? {
            RuleTypes.values().forEach {
                if (it.name == name) {
                    return it
                }
            }
            return null
        }
    }
}

interface JsFields {
    fun get(): List<FieldData>
}
