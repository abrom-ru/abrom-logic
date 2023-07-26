package rules.custom.data

import Logger
import models.expressionSolver.ExpressionBuilder
import models.expressionSolver.ExpressionException
import models.js.CellType
import models.toTopic
import rules.JsFields
import rules.Rule
import rules.RuleInfo
import rules.custom.CustomRule
import rules.custom.data.CustomRuleData.Fields.IS_WITH_STATE
import rules.custom.data.CustomRuleData.Fields.OFF_CONDITION
import rules.custom.data.CustomRuleData.Fields.OFF_DELAY
import rules.custom.data.CustomRuleData.Fields.OFF_MESSAGE
import rules.custom.data.CustomRuleData.Fields.ON_CONDITION
import rules.custom.data.CustomRuleData.Fields.ON_DELAY
import rules.custom.data.CustomRuleData.Fields.ON_MESSAGE
import rules.custom.data.CustomRuleData.Fields.OUTPUT_TOPICS
import rules.data.DataType
import rules.data.FieldData
import rules.data.RuleData
import java.util.Locale

open class CustomRuleData(override val info: RuleInfo) : RuleData {
    override val fields: MutableMap<FieldData, String> = Fields.values().associateWith { "" }.toMutableMap()
    override val values: List<FieldData> = Fields.values().toList()

    override fun toRule(): Rule? {
        if (!dataIsCorrect()) return null

        val onExpression = try {
            ExpressionBuilder(fields[ON_CONDITION] ?: "").getExpression()
        } catch (ex: ExpressionException) {
            Logger.error {
                "can't convert custom rule ${ex.message}"
            }
            return null
        }
        val offExpression = if ((fields[OFF_CONDITION] ?: "").isBlank()) {
            null
        } else {
            try {
                ExpressionBuilder(fields[OFF_CONDITION] ?: "").getExpression()
            } catch (ex: ExpressionException) {
                Logger.error {
                    ex.message
                }
                null
            }
        }
        val onMessage = try {
            ExpressionBuilder(fields[ON_MESSAGE] ?: "").getExpression()
        } catch (ex: ExpressionException) {
            Logger.error {
                "can't convert custom rule ${ex.message}"
            }
            return null
        }
        val offMessage = if ((fields[OFF_MESSAGE] ?: "").isBlank()) {
            null
        } else {
            try {
                ExpressionBuilder(fields[OFF_MESSAGE] ?: "").getExpression()
            } catch (ex: ExpressionException) {
                Logger.error {
                    "can't convert custom rule ${ex.message}"
                }
                null
            }
        }

        return CustomRule(
            (fields[IS_WITH_STATE]?.isBlank() == true) || (fields[IS_WITH_STATE]?.toIntOrNull() ?: return null) == 1,
            onExpression,
            offExpression,
            fields[ON_DELAY]?.toIntOrNull() ?: 0,
            fields[OFF_DELAY]?.toIntOrNull() ?: 0,
            onMessage,
            offMessage,
            fields[OUTPUT_TOPICS]?.split(',')?.map { it.toTopic() } ?: return null,

        )
    }

    private fun dataIsCorrect(): Boolean {
        return !(fields[OUTPUT_TOPICS]?.isBlank() ?: true)
    }

    enum class Fields : FieldData {
        ON_CONDITION {
            override val label: String = "Условие на включение"
            override val dataType: DataType = DataType.STRING
        },
        OFF_CONDITION {
            override val label: String = "Условие на выключение"
            override val dataType: DataType = DataType.STRING
        },
        ON_DELAY {
            override val label: String = "Заддержка на включение(сек)"
            override val dataType: DataType = DataType.INT
        },
        OFF_DELAY {
            override val label: String = "Заддержка на выключение(сек)"
            override val dataType: DataType = DataType.INT
        },
        ON_MESSAGE {
            override val label: String = "Значение при включение"
            override val dataType: DataType = DataType.STRING
        },
        OFF_MESSAGE {
            override val label: String = "Значение при выключение"
            override val dataType: DataType = DataType.STRING
        },
        OUTPUT_TOPICS {
            override val label: String = "Управляемые топики"
            override val dataType: DataType = DataType.STRING
        },
        IS_WITH_STATE {
            override val label: String = "Поддерживание последнего состояние"
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

        override fun get(): List<FieldData> = Fields.values().toList()
    }
}
