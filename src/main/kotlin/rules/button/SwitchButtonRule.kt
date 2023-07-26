package rules.button

import models.Topic
import models.expressionSolver.Expression
import storage.TopicBase

class SwitchButtonRule(
    buttonTopic: Topic,
    private val tapType: TapType,
    outputTopics: List<Topic>,
    onDelayMillis: Long = 0,
    offDelayMillis: Long = 0,
    onValue: Expression,
    offValue: Expression,
    onCondition: Expression?,

) : ButtonRule(outputTopics, onDelayMillis, offDelayMillis, onValue, offValue, onCondition) {

    private val button = LightSwitch(buttonTopic) { checkRule() }
    private fun checkRule() {
        if (button.checkTap(tapType)) {
            changeState()
        }
    }

    override fun deleteRule() {
        TopicBase.removeCallback(button::checkButton)
    }

    init {
        TopicBase.addCallback(
            (outputTopics + onValue.getTopics() + offValue.getTopics()) + buttonTopic,
            button::checkButton,
        )
    }
}
