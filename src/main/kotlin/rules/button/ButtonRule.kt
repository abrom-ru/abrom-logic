package rules.button

import Logger
import models.RangeDevice
import models.TaskManager
import models.Topic
import models.expressionSolver.Expression
import models.expressionSolver.ExpressionException
import models.expressionSolver.LogicValue
import models.expressionSolver.NumberValue
import rules.Rule

abstract class ButtonRule(
    outputTopics: List<Topic>,
    private val onDelayMillis: Long = 0,
    private val offDelayMillis: Long = 0,
    private val onValue: Expression,
    private val offValue: Expression,
    private val onCondition: Expression?,
) : Rule {

    private var curState: Boolean = false
    private val outputDevices = outputTopics.map { RangeDevice(it) }
    private val taskManager = TaskManager()

    private fun getExpressionValue(expression: Expression): Int {
        return (
            expression.solve() as? NumberValue
                ?: throw ExpressionException("find Logic Value, but need NumberValue")
            ).value.toInt()
    }

    private fun updateState() {
        if (onCondition != null) {
            Logger.debug { "cur state is $curState" }
            curState = (
                onCondition.solve() as? LogicValue
                    ?: throw ExpressionException("find Number Value, but need Logic value")
                ).value
            Logger.debug { "new state is $curState" }
        }
    }

    protected fun changeState() {
        updateState()
        curState = !curState
        changeWithDelay(curState)
    }

    private fun changeWithDelay(state: Boolean) {
        taskManager.add(getStateDelay(state)) {
            try {
                setValue(getExpressionValue(getStateValue(state)))
            } catch (ex: ExpressionException) {
                Logger.error {
                    "ERROR in button rule with ${ex.message}"
                }
            }
        }
    }

    private fun setValue(value: Int) {
        outputDevices.forEach {
            it.value = value
        }
    }

    private fun getStateDelay(state: Boolean) = if (state) onDelayMillis else offDelayMillis

    private fun getStateValue(state: Boolean) = if (state) onValue else offValue

    init {
        changeWithDelay(false)
    }
}
