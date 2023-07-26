package rules.telegram

import Logger
import models.expressionSolver.Expression
import models.expressionSolver.ExpressionException
import models.expressionSolver.LogicValue
import rules.Rule
import storage.TopicBase

class TelegramRule(
    private val keys: List<String>,
    private val sendCondition: Expression,
    private val message: String,
) : Rule {
    private val sender = TelegramSender()
    var state: Boolean = false

    private fun checkRule() {
        val needToSend = getExpressionValue()
        if (needToSend && !state) {
            state = true
            keys.forEach {
                sender.send(it, message)
            }
        } else if (!needToSend && state) {
            state = false
        }
    }

    override fun deleteRule() {
        TopicBase.removeCallback(::checkRule)
    }

    private fun getExpressionValue(): Boolean {
        val res: Boolean = try {
            (
                sendCondition.solve() as? LogicValue
                    ?: throw ExpressionException("find NumberValue instead LogicValue")
                ).value
        } catch (ex: ExpressionException) {
            Logger.error {
                "expression error with ${ex.message} in telegram rule"
            }
            false
        }
        return res
    }

    init {
        TopicBase.addCallback(sendCondition.getTopics(), ::checkRule)
    }
}
