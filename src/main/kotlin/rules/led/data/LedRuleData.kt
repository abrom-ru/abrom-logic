package rules.led.data

import models.Topic.Companion.isTopicCorrect
import models.js.CellType
import models.toTopic
import rules.JsFields
import rules.Rule
import rules.RuleInfo
import rules.data.DataType
import rules.data.FieldData
import rules.data.RuleData
import rules.led.LedRule
import rules.led.data.LedRuleData.Fields.BUTTON
import rules.led.data.LedRuleData.Fields.LED
import rules.led.data.LedRuleData.Fields.MAX
import rules.led.data.LedRuleData.Fields.SWITCH
import rules.led.data.LedRuleData.Fields.values
import java.util.Locale

open class LedRuleData(override val info: RuleInfo) : RuleData {
    override val fields: MutableMap<FieldData, String> = Fields.values().associateWith { "" }.toMutableMap()
    override val values: List<FieldData> = Fields.values().toList()

    override fun toRule(): Rule? {
        if (!isTopicCorrect(fields[BUTTON] ?: return null) || !isTopicCorrect(
                fields[LED] ?: return null,
            )
        ) {
            return null
        }
        return LedRule(
            fields[BUTTON]?.toTopic() ?: return null,
            fields[LED]?.toTopic() ?: return null,
            fields[MAX]?.toIntOrNull() ?: 255,
            if (fields[SWITCH]?.isNotBlank() == true) fields[SWITCH]?.toTopic() else null,
        )
    }

    companion object : JsFields {
        override fun get() = values().toList()
    }

    enum class Fields : FieldData {
        BUTTON {
            override val dataType: DataType = DataType.TOPIC
            override val label: String = "Топик выключателя"
        },
        LED {
            override val dataType: DataType = DataType.TOPIC
            override val label: String = "Топик ленты"
        },
        MAX {
            override val dataType: DataType = DataType.INT
            override val label: String = "Максимальная яркость"
        },

        SWITCH {
            override val label: String = "Переключатель ленты"
            override val dataType: DataType = DataType.TOPIC
        }, ;

        override val dbName: String = toString()
        override val jsName: String = toString()
        override val type: CellType = CellType.TEXT

        override fun toString(): String {
            return super.toString().split("_").joinToString(separator = " ") { it.lowercase(Locale.getDefault()) }
        }
    }
}
