package rules.climateControl.data

import models.Topic
import models.js.Cell
import models.js.CellType
import models.js.VirtualDevice
import models.toTopic
import rules.JsFields
import rules.Rule
import rules.RuleInfo
import rules.climateControl.DeviceType
import rules.climateControl.HomeBuilder
import rules.climateControl.Room
import rules.climateControl.data.RoomData.Fields.CUR_FLOOR_TEMP
import rules.climateControl.data.RoomData.Fields.CUR_TEMP
import rules.climateControl.data.RoomData.Fields.FLOOR_INVERTED
import rules.climateControl.data.RoomData.Fields.FLOOR_RELAY
import rules.climateControl.data.RoomData.Fields.HEAT_INVERTED
import rules.climateControl.data.RoomData.Fields.HEAT_RELAY
import rules.climateControl.data.RoomData.Fields.MAX_FLOOR_TEMP
import rules.data.DataType
import rules.data.FieldData
import rules.data.RuleData
import java.util.Locale

open class RoomData(final override val info: RuleInfo) : RuleData {
    override val fields: MutableMap<FieldData, String> = Fields.values().associateWith { "" }.toMutableMap()
    override val values: List<FieldData> = Fields.values().toList()
    var prefTemp = Topic.empty
    var curJsTemp = Topic.empty
    var minFloorTemp = Topic.empty
    var comfortFloorMode = Topic.empty
    var conditionerState = Topic.empty

    override fun toRule(): Room? {
        HomeBuilder.fillHomeDevices(this)
        // if (conditioner.isNotEmpty()) {
        //     conditioner.forEach {
        //         it.builder.setHeatMode(heatMode).setWinterMode(winterMode).setForceShotDown(forceShutdown)
        //             .setNobodyAtHome(nobodyAtHome).setMqttState(conditionerState).setCurTemp(fields[CUR_TEMP].toTopic())
        //             .setPrefTemp(prefTemp)
        //     }
        // }

        return Room(
            info,
            fields[FLOOR_RELAY]?.split(",")?.map { it.toTopic() }?.filter { it != Topic.empty }
                ?: return closeRuleBuilding(),
            fields[HEAT_RELAY]?.split(",")?.map { it.toTopic() }?.filter { it != Topic.empty }
                ?: return closeRuleBuilding(),
            fields[CUR_FLOOR_TEMP].toTopic(),
            fields[CUR_TEMP].toTopic().also { if (it == Topic.empty) return@toRule closeRuleBuilding() },
            curJsTemp,
            minFloorTemp,
            fields[MAX_FLOOR_TEMP]?.toIntOrNull() ?: defaultHighTemp,
            prefTemp,
            comfortFloorMode,
            (fields[FLOOR_INVERTED] == "1" || fields[FLOOR_INVERTED]!!.isBlank()),
            (fields[HEAT_INVERTED] == "1" || fields[HEAT_INVERTED]!!.isBlank()),
        )
    }

    private fun closeRuleBuilding(): Room? {
        Rule.builder.delete(pageName)
        return null
    }

    fun getVirtualDevice(): VirtualDevice {
        val list = mutableListOf(DeviceType.PREF_TEMP, DeviceType.CUR_TEMP)
        return VirtualDevice(
            pageName,
            info.name,
            list.map { Cell(it) }.toMutableList(),
        )
    }

    private val pageName
        get() = "${info.name}_room"

    enum class Fields : FieldData {
        CUR_TEMP {

            override val label: String = "Термометр воздуха"
            override val dataType: DataType = DataType.TOPIC
        },
        CUR_FLOOR_TEMP {

            override val label: String = "Термометр пола"
            override val dataType: DataType = DataType.TOPIC
        },
        FLOOR_RELAY {
            override val label: String = "Реле пола"
            override val dataType: DataType = DataType.STRING
        },
        HEAT_RELAY {
            override val label: String = "Реле батарей"
            override val dataType: DataType = DataType.STRING
        },
        MAX_FLOOR_TEMP {
            override val label: String = "Максимальная температура пола"
            override val dataType: DataType = DataType.INT
        },
        FLOOR_INVERTED {
            override val label: String = "Инверсия реле пола"
            override val dataType: DataType = DataType.BOOLEAN
        },
        HEAT_INVERTED {
            override val label: String = "Инверсия реле батарей"
            override val dataType: DataType = DataType.BOOLEAN
        },
        ;

        override val dbName: String = toString()
        override val jsName: String = toString()
        override val type: CellType = CellType.TEXT
        override fun toString(): String {
            return super.toString().split("_").joinToString(separator = " ") { it.lowercase(Locale.getDefault()) }
        }
    }

    companion object : JsFields {
        private const val defaultHighTemp = 40
        override fun get(): List<FieldData> {
            return Fields.values().toList()
        }
    }
}
