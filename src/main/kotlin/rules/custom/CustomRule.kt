package rules.custom

import Logger
import models.TaskManager
import models.TextDevice
import models.Topic
import models.expressionSolver.ExNumber
import models.expressionSolver.Expression
import models.expressionSolver.ExpressionException
import models.expressionSolver.LogicValue
import models.expressionSolver.NumberValue
import rules.Rule
import storage.TopicBase
import javax.management.timer.Timer

class CustomRule(
    private val isWithState: Boolean,
    private val onCondition: Expression,
    private val offCondition: Expression?,
    private val onDelay: Int = 0,
    private val offDelay: Int?,
    private val onMessage: Expression = Expression(listOf(ExNumber(1))),
    private val offMessage: Expression?,
    outputTopics: List<Topic>,
) : Rule {

    private val timer = TaskManager()

    private var curState: Boolean? = null
    private val outputTopics = outputTopics.map { TextDevice(it) }
    private fun send(message: String, delay: Int) {
        // Logger.info {
        //     "ready to send $message after $delay second"
        // }
        timer.add(delay * Timer.ONE_SECOND) {
            outputTopics.forEach {
                it.value = message
            }
        }
    }

    private fun getOutputValue(expression: Expression): Number {
        val res: Number = try {
            val solve = expression.solve()
            if (solve is NumberValue) {
                solve.value
            } else {
                if ((solve as LogicValue).value) 1 else 0
            }
        } catch (ex: ExpressionException) {
            Logger.error {
                "error with expression solve in custom logic"
            }
            Double.NaN
        }
        return res
    }

    private fun getConditionValue(expression: Expression?): Boolean {
        val res: Boolean = try {
            (
                expression?.solve() as? LogicValue
                    ?: throw ExpressionException("find number value instead logic value")
                ).value
        } catch (ex: ExpressionException) {
            Logger.error {
                "error with expression solve in custom logic"
            }
            false
        }
        return res
    }

    private fun singleCondition() {
        val onConditionStatus: Boolean = getConditionValue(onCondition)
        val message = getOutputValue(onMessage).toString()
        if ((!isWithState || (curState == false)) && onConditionStatus) {
            curState = true
            send(message, onDelay)
        } else if (isWithState && !onConditionStatus) {
            timer.clear()
            curState = false
        }
    }

    private fun checkRule() {
        if (offDelay == null || offCondition == null || offMessage == null) {
            singleCondition()
        } else {
            val onConditionStatus: Boolean = getConditionValue(onCondition)
            val offConditionStatus: Boolean = getConditionValue(offCondition)
            if (onConditionStatus && offConditionStatus) {
                Logger.error {
                    "conditions in $this rule conflict"
                }
                return
            }
            if ((onConditionStatus && curState == true) || (offConditionStatus && curState == false)) return
            val message = (
                if (onConditionStatus) {
                    getOutputValue(onMessage)
                } else {
                    getOutputValue(offMessage)
                }
                ).toString()
            if (onConditionStatus) curState = true
            if (offConditionStatus) curState = false
            if (onConditionStatus || offConditionStatus) send(message, if (onConditionStatus) onDelay else offDelay)
        }
    }

    override fun toString(): String {
        return "CustomRule(onCondition=$onCondition, offCondition=$offCondition, onDelay=$onDelay, offDelay=$offDelay, onMessage='$onMessage', offMessage='$offMessage', outputTopics=$outputTopics)"
    }

    override fun deleteRule() {
        TopicBase.removeCallback(::checkRule)
    }

    init {
        val topics: List<Topic> =
            onCondition.getTopics() + outputTopics + onMessage.getTopics() + (
                offCondition?.getTopics()
                    ?: listOf()
                ) + (offMessage?.getTopics() ?: listOf())
        TopicBase.addCallback(topics, ::checkRule)
    }
}
