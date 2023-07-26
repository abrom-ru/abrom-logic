package rules.climateControl.data

import models.Topic
import models.js.CellType
import models.toTopic
import rules.JsFields
import rules.Rule
import rules.RuleInfo
import rules.climateControl.FloorController
import rules.data.DBData
import rules.data.DataType
import rules.data.FieldData
import java.util.Locale

open class FloorControllerData(
    override val info: RuleInfo,
) : DBData {

    override val fields: MutableMap<FieldData, String> = Fields.values().associateWith { "" }.toMutableMap()

    override val values: List<FieldData> = Fields.values().toList()
    override fun toRule(): Rule? {
        val isInverted = fields[Fields.INVERTED]?.toIntOrNull() == 1
        val relays = fields[Fields.RELAYS]!!.split(",").map { it.toTopic() }
        val roomTemp = fields[Fields.ROOM_TEMP].toTopic()
        val isWater = fields[Fields.IS_WATER]?.toIntOrNull() == 1
        val floorTemp = fields[Fields.FLOOR_TEMP].toTopic()
        if (relays.contains(Topic.empty)) return null
        return FloorController(
            ruleInfo = info,
            inverted = isInverted,
            floorTemp = floorTemp,
            relays = relays,
            roomTemp = roomTemp,
            isWater = isWater,
        )
    }

    enum class Fields : FieldData {
        RELAYS,
        FLOOR_TEMP, ROOM_TEMP, INVERTED, IS_WATER;

        override val type: CellType = CellType.TEXT

        override val jsName: String = toString()

        override val dbName: String = toString()

        override val dataType: DataType = DataType.STRING

        override val label: String
            get() = "todo()"

        override fun toString(): String {
            return super.toString().split("_").joinToString(separator = " ") { it.lowercase(Locale.getDefault()) }
        }
    }

    companion object : JsFields {
        override fun get(): List<FieldData> {
            return Fields.values().toList()
        }
    }
}
