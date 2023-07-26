package rules.rgb

import kotlinx.coroutines.delay
import models.RangeDevice
import models.Topic
import rules.Rule
import rules.button.LightSwitch
import rules.button.TapType
import storage.TopicBase
import kotlin.math.roundToInt

class RGBLedRule(
    redTopic: Topic,
    greenTopic: Topic,
    blueTopic: Topic,
    buttonTopic: Topic,
    private val tapType: TapType,
    private val maxValue: Int,
) : Rule {
    private var red: Int by RangeDevice(redTopic)
    private var green: Int by RangeDevice(greenTopic)
    private var blue: Int by RangeDevice(blueTopic)
    private val button = LightSwitch(buttonTopic) { checkRule() }

    private var state = false

    private suspend fun transfusion() {
        var color = 0
        while (state) {
            color += 10
            val (r, g, b) = when {
                color <= LED_MAX -> Triple(LED_MAX, color, 0)
                color in 256..510 -> Triple(510 - color, LED_MAX, 0)
                color in 511..765 -> Triple(0, LED_MAX, color - 510)
                color in 766..1020 -> Triple(0, 1020 - color, LED_MAX)
                color in 1021..1275 -> Triple(color - 1020, 0, LED_MAX)
                color in 1276..1530 -> Triple(LED_MAX, 0, 1530 - color)
                else -> Triple(0, 0, 0)
            }
            red = (r.toDouble() / LED_MAX * maxValue).roundToInt()
            green = (g.toDouble() / LED_MAX * maxValue).roundToInt()
            blue = (b.toDouble() / LED_MAX * maxValue).roundToInt()
            color %= 1530
            delay(DELAY)
        }
        red = 0
        green = 0
        blue = 0
    }

    private fun checkRule() {
        if (button.checkTap(tapType)) {
            state = !state
            if (state) {
                Rule.addTask { transfusion() }
            }
        }
    }

    override fun deleteRule() {
        TopicBase.removeCallback(button::checkButton)
    }

    init {
        TopicBase.addCallback(listOf(buttonTopic), (button::checkButton))
        this.red = 0
        this.green = 0
        this.blue = 0
    }

    companion object {
        private const val DELAY = 50L
        private const val LED_MAX = 255
    }
}
