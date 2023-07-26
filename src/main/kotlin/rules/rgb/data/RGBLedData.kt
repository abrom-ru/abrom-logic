package rules.rgb.data

import models.js.CellType
import models.toTopic
import rules.JsFields
import rules.Rule
import rules.RuleInfo
import rules.button.TapType
import rules.data.DataType
import rules.data.FieldData
import rules.data.RuleData
import rules.rgb.RGBLedRule
import rules.rgb.data.RGBLedData.Companion.Fields.BLUE
import rules.rgb.data.RGBLedData.Companion.Fields.BUTTON
import rules.rgb.data.RGBLedData.Companion.Fields.GREEN
import rules.rgb.data.RGBLedData.Companion.Fields.MAX
import rules.rgb.data.RGBLedData.Companion.Fields.RED
import rules.rgb.data.RGBLedData.Companion.Fields.TAP_TYPE
import rules.rgb.data.RGBLedData.Companion.Fields.values
import java.util.Locale

open class RGBLedData(override val info: RuleInfo) : RuleData {
    override val values: List<FieldData> = Fields.values().toList()
    override val fields: MutableMap<FieldData, String> = Fields.values().associateWith { "" }.toMutableMap()

    override fun toRule(): Rule? {
        return RGBLedRule(
            fields[RED]?.toTopic() ?: return null,
            fields[GREEN]?.toTopic() ?: return null,
            fields[BLUE]?.toTopic() ?: return null,
            fields[BUTTON]?.toTopic() ?: return null,
            try {
                TapType.valueOf(fields[TAP_TYPE] ?: return null)
            } catch (ex: IllegalArgumentException) {
                return null
            },
            fields[MAX]?.toIntOrNull() ?: return null,
        )
    }

    companion object : JsFields {
        override fun get() = values().map { it }
        enum class Fields : FieldData {
            BUTTON {
                override val label: String = "Топик выключателя"
                override val dataType: DataType = DataType.TOPIC
            },
            RED {

                override val label: String = "Топик красного"
                override val dataType: DataType = DataType.TOPIC
            },
            GREEN {

                override val label: String = "Топик  зелёного"
                override val dataType: DataType = DataType.TOPIC
            },
            BLUE {

                override val label: String = "Топик синего"
                override val dataType: DataType = DataType.TOPIC
            },
            TAP_TYPE {

                override val label: String = "Тип нажатия"
                override val dataType: DataType = DataType.STRING
            },
            MAX {
                override val label: String = "Максимальная яркость"
                override val dataType: DataType = DataType.INT
            }, ;

            override val dbName: String
                get() = this.toString()
            override val jsName: String = this.toString()
            override val type: CellType
                get() = CellType.TEXT

            override fun toString(): String {
                return super.toString().split("_").joinToString(separator = " ") { it.lowercase(Locale.getDefault()) }
            }
        }
    }
}
