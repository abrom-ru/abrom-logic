package rules.watering.data

import models.js.CellType
import models.toTopic
import rules.JsFields
import rules.Rule
import rules.RuleInfo
import rules.data.DataType
import rules.data.FieldData
import rules.data.RuleData
import rules.watering.WateringRule
import rules.watering.data.WateringRuleData.Fields.LOWER_TRAILER
import rules.watering.data.WateringRuleData.Fields.PUMP
import rules.watering.data.WateringRuleData.Fields.TAP
import rules.watering.data.WateringRuleData.Fields.TOP_TRAILER
import rules.watering.data.WateringRuleData.Fields.VALVES
import rules.watering.data.WateringRuleData.Fields.values
import java.util.Locale

open class WateringRuleData(override val info: RuleInfo) : RuleData {
    override val fields: MutableMap<FieldData, String> = Fields.values().associateWith { "" }.toMutableMap()
    override val values: List<FieldData> = Fields.values().toList()

    enum class Fields : FieldData {
        TAP {
            override val label: String = "Топик крана"
            override val dataType: DataType = DataType.TOPIC
        },
        TOP_TRAILER {
            override val label: String = "Верхний концевик"
            override val dataType: DataType = DataType.TOPIC
        },
        VALVES {
            override val label: String = "Каналы полива"
            override val dataType: DataType = DataType.STRING
        },
        LOWER_TRAILER {
            override val label: String = "Нижний концевик"
            override val dataType: DataType = DataType.TOPIC
        },
        PUMP {
            override val label: String = "Топик насоса"
            override val dataType: DataType = DataType.TOPIC
        }, ;

        override val dbName: String
            get() = toString()
        override val jsName: String
            get() = toString()
        override val type: CellType
            get() = CellType.TEXT

        override fun toString(): String {
            return super.toString().split("_").joinToString(separator = " ") {
                it.replaceFirstChar {
                    if (it.isLowerCase()) {
                        it.titlecase(
                            Locale.getDefault(),
                        )
                    } else {
                        it.toString()
                    }
                }
            }
        }
    }

    override fun toRule(): Rule? {
        val valves = fields[VALVES]?.split(",")?.map { it.trim().toTopic() } ?: return null
        val lowerTrailer = if (!fields[LOWER_TRAILER].isNullOrBlank()) fields[LOWER_TRAILER]?.toTopic() else null
        return WateringRule(
            info.name,
            fields[TAP]!!.toTopic(),
            fields[TOP_TRAILER]!!.toTopic(),
            lowerTrailer,
            valves,
            fields[PUMP]!!.toTopic(),
        )
    }

    companion object : JsFields {
        override fun get() = values().toList()
    }
}
