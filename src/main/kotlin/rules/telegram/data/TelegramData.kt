package rules.telegram.data

import Logger
import models.expressionSolver.ExpressionBuilder
import models.expressionSolver.ExpressionException
import models.js.CellType
import rules.JsFields
import rules.Rule
import rules.RuleInfo
import rules.data.DataType
import rules.data.FieldData
import rules.data.RuleData
import rules.telegram.TelegramRule
import rules.telegram.data.TelegramData.Companion.Fields.KEYS
import rules.telegram.data.TelegramData.Companion.Fields.MESSAGE
import rules.telegram.data.TelegramData.Companion.Fields.SEND_CONDITION
import rules.telegram.data.TelegramData.Companion.Fields.values
import java.util.Locale

open class TelegramData(override val info: RuleInfo) : RuleData {
    override val fields: MutableMap<FieldData, String> = Fields.values().associateWithTo(mutableMapOf()) { "" }
    override val values: List<FieldData> = Fields.values().toList()

    override fun toRule(): Rule? {
        val keys = fields[KEYS]?.split(",")?.map { it.trim() } ?: return null
        val condition = try {
            ExpressionBuilder(fields[SEND_CONDITION] ?: return null).getExpression()
        } catch (ex: ExpressionException) {
            Logger.error {
                "expression exception $ex in telegram rule"
            }
            return null
        }
        val message = fields[MESSAGE] ?: return null
        return TelegramRule(keys, condition, message)
    }

    companion object : JsFields {

        override fun get() = values().toList()

        enum class Fields : FieldData {
            KEYS {
                override val label: String = "Ключи телеграма"
                override val dataType: DataType = DataType.STRING
            },
            SEND_CONDITION {
                override val label: String = "Условие для отправки"
                override val dataType: DataType = DataType.STRING
            },
            MESSAGE {
                override val label: String = "Сообщение"
                override val dataType: DataType = DataType.STRING
            }, ;

            override val dbName: String = toString()
            override val jsName: String = toString()
            override val type: CellType = CellType.TEXT

            override fun toString(): String {
                return super.toString().split("_").joinToString(separator = " ") { it.lowercase(Locale.getDefault()) }
            }
        }
    }
}
