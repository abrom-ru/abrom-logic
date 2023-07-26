package rules.climateControl.conditioner.ir

import models.Topic
import models.js.CellType
import models.toTopic
import rules.JsFields
import rules.RuleInfo
import rules.climateControl.HomeBuilder
import rules.climateControl.conditioner.ConditionerData
import rules.data.DataType
import rules.data.FieldData
import java.util.Locale

open class IRConditionerData(info: RuleInfo) : ConditionerData(info) {

    override val fields: MutableMap<FieldData, String> = Fields.values().associateWith { "" }.toMutableMap()
    override val builder = IRConditioner.Builder()

    /**
     * Complete rule data with all needed fields
     *
     * @return true if data is complete and false else
     */
    override fun complete(): Boolean {
        builder.setDevice(Topic(fields[Fields.DEVICE]!!, ""))
        builder.setOutsideTemp(fields[Fields.OUTSIDE_TEMP].toTopic())
        builder.setRoomTemp(fields[Fields.ROOM_TEMP].toTopic())
        builder.conditionerTemp = fields[Fields.CONDITIONER_TEMP].toTopic()
        HomeBuilder.addConditionerDevices(info, builder)
        return builder.device != Topic.empty
    }

    override val values: List<FieldData> = Fields.values().toList()

    enum class Fields : FieldData {
        DEVICE {
            override val label: String = "Название кондиционера"
            override val dataType: DataType = DataType.TOPIC
        },
        OUTSIDE_TEMP {
            override val label: String = "Термометр улицы"
            override val dataType: DataType = DataType.TOPIC
        },
        ROOM_TEMP {
            override val label: String = "Температура в комнате"
            override val dataType: DataType = DataType.TOPIC
        },
        CONDITIONER_TEMP {
            override val label: String = "Температура кондиционера"
            override val dataType: DataType = DataType.TOPIC
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
        override fun get(): List<FieldData> = Fields.values().toList()
    }
}
