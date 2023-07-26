package rules.button

import models.Topic
import models.expressionSolver.Expression
import models.expressionSolver.LogicValue
import storage.TopicBase

class ConditionButtonRule(
    outputTopics: List<Topic>,
    private val changeCondition: Expression,
    onDelayMillis: Long = 0,
    offDelayMillis: Long = 0,
    onValue: Expression,
    offValue: Expression,
    onCondition: Expression?,
) : ButtonRule(outputTopics, onDelayMillis, offDelayMillis, onValue, offValue, onCondition) {
    private fun checkRule() {
        if (getExpressionValue()) {
            changeState()
        }
    }

    private fun getExpressionValue(): Boolean {
        val solution = changeCondition.solve()
        return (solution as? LogicValue)?.value ?: false
    }

    override fun deleteRule() {
        TopicBase.removeCallback(::checkRule)
    }

    init {
        TopicBase.addCallback(changeCondition.getTopics(), ::checkRule)
    }
}
