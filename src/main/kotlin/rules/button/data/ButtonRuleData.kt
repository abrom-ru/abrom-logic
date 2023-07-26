package rules.button.data

import models.Topic
import models.Topic.Companion.isTopicCorrect
import models.expressionSolver.ExpressionBuilder
import models.expressionSolver.ExpressionException
import models.js.CellType
import models.toTopic
import rules.JsFields
import rules.Rule
import rules.RuleInfo
import rules.button.ConditionButtonRule
import rules.button.SwitchButtonRule
import rules.button.TapType
import rules.button.data.ButtonRuleData.Companion.Fields.BUTTON_TOPIC
import rules.button.data.ButtonRuleData.Companion.Fields.CHANGE_CONDITION
import rules.button.data.ButtonRuleData.Companion.Fields.OFF_DELAY
import rules.button.data.ButtonRuleData.Companion.Fields.OFF_VALUE
import rules.button.data.ButtonRuleData.Companion.Fields.ON_CONDITION
import rules.button.data.ButtonRuleData.Companion.Fields.ON_DELAY
import rules.button.data.ButtonRuleData.Companion.Fields.ON_VALUE
import rules.button.data.ButtonRuleData.Companion.Fields.OUTPUT_TOPIC
import rules.button.data.ButtonRuleData.Companion.Fields.TAP_TYPE
import rules.data.DataType
import rules.data.FieldData
import rules.data.RuleData
import java.util.Locale

open class ButtonRuleData(override val info: RuleInfo) : RuleData {
    override val values: List<FieldData> = Fields.values().toList()
    override val fields: MutableMap<FieldData, String> = Fields.values().associateWith { "" }.toMutableMap()
    private lateinit var outputTopics: List<Topic>
    override fun toRule(): Rule? {
        var isDataCorrect = isAllFieldsPresent()

        fields[OUTPUT_TOPIC]?.split(",")?.forEach {
            if (!isTopicCorrect(it)) {
                isDataCorrect = false
            }
        }
        outputTopics =
            fields[OUTPUT_TOPIC]?.split(",")?.filter { it.isNotBlank() }?.map { Topic(it.trim()) } ?: return null
        return if (!isDataCorrect) {
            null
        } else if (fields[TAP_TYPE]!!.isBlank() || fields[BUTTON_TOPIC]!!.isBlank()) {
            buildConditionRule()
        } else {
            buildSwitchRule()
        }
    }

    private fun buildSwitchRule(): Rule? {
        return if (!TapType.values().map { it.name }.contains(fields[TAP_TYPE])) {
            null
        } else {
            SwitchButtonRule(
                fields[BUTTON_TOPIC]?.toTopic() ?: return null,
                try {
                    TapType.valueOf(fields[TAP_TYPE] ?: return null)
                } catch (ex: IllegalArgumentException) {
                    return null
                },
                outputTopics,
                fields[ON_DELAY]?.toLongOrNull() ?: 0,
                fields[OFF_DELAY]?.toLongOrNull() ?: 0,
                try {
                    ExpressionBuilder(fields[ON_VALUE] ?: return null).getExpression()
                } catch (ex: ExpressionException) {
                    return null
                },
                try {
                    ExpressionBuilder(fields[OFF_VALUE] ?: return null).getExpression()
                } catch (ex: ExpressionException) {
                    return null
                },
                if (fields[ON_CONDITION]?.isBlank() == true) {
                    null
                } else {
                    try {
                        ExpressionBuilder(fields[ON_CONDITION] ?: return null).getExpression()
                    } catch (ex: ExpressionException) {
                        return null
                    }
                },
            )
        }
    }

    private fun buildConditionRule(): Rule? {
        return ConditionButtonRule(
            outputTopics,
            try {
                ExpressionBuilder(fields[CHANGE_CONDITION] ?: return null).getExpression()
            } catch (
                ex: ExpressionException,
            ) {
                return null
            },
            fields[ON_DELAY]?.toLongOrNull() ?: 0,
            fields[OFF_DELAY]?.toLongOrNull() ?: 0,
            try {
                ExpressionBuilder(fields[ON_VALUE] ?: return null).getExpression()
            } catch (ex: ExpressionException) {
                return null
            },
            try {
                ExpressionBuilder(fields[OFF_VALUE] ?: return null).getExpression()
            } catch (ex: ExpressionException) {
                return null
            },
            if (fields[ON_CONDITION]?.isBlank() == true) {
                null
            } else {
                try {
                    ExpressionBuilder(fields[ON_CONDITION] ?: return null).getExpression()
                } catch (ex: ExpressionException) {
                    return null
                }
            },
        )
    }

    private fun isAllFieldsPresent(): Boolean {
        var result = true
        Fields.values().forEach {
            if (fields[it] == null) {
                result = false
            }
        }
        return result
    }

    companion object : JsFields {

        enum class Fields : FieldData {
            BUTTON_TOPIC {

                override val label: String = "Топик выключателя"
                override val dataType: DataType = DataType.TOPIC
            },
            TAP_TYPE {

                override val label: String = "Тип нажатие"
                override val dataType: DataType = DataType.STRING
            },
            OUTPUT_TOPIC {

                override val label: String = "Управляемые топики"
                override val dataType: DataType = DataType.STRING
            },
            ON_DELAY {

                override val label: String = "Задержка на включение"
                override val dataType: DataType = DataType.STRING
            },
            OFF_DELAY {

                override val label: String = "Задержка на выключение"
                override val dataType: DataType = DataType.STRING
            },
            ON_VALUE {

                override val label: String = "Значение на включение"
                override val dataType: DataType = DataType.STRING
            },
            OFF_VALUE {
                override val label: String = "Значение на включение"
                override val dataType: DataType = DataType.STRING
            },
            ON_CONDITION {
                override val label: String = "Проверка Включен ли"
                override val dataType: DataType = DataType.STRING
            },
            CHANGE_CONDITION {
                override val label: String = "Условие на изменение состояние"
                override val dataType: DataType = DataType.STRING
            },
            ;

            override val dbName: String = toString()
            override val jsName: String = toString()
            override val type: CellType = CellType.TEXT

            override fun toString(): String {
                return super.toString().split("_").joinToString(separator = " ") { it.lowercase(Locale.getDefault()) }
            }
        }

        override fun get(): List<FieldData> = Fields.values().toList()
    }
}
