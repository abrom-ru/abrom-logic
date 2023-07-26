package rules.led

import kotlinx.coroutines.delay
import models.SwitchDevice
import models.Topic
import rules.Rule
import rules.button.LightSwitch
import storage.TopicBase
import kotlin.math.roundToInt

class LedRule(buttonTopic: Topic, ledTopic: Topic, maxValue: Int, switchTopic: Topic?) : Rule {
    private val button = LightSwitch(buttonTopic) { checkRule() }
    private var led = Led(ledTopic, maxValue).apply { turnOn() }
    private var speed = 1
    private var curLevel = 0
    private val ledSwitch: SwitchDevice? = if (switchTopic != null) SwitchDevice(switchTopic) else null

    // TODO Add more values
    private val brights = listOf(
        20, 23, 25, 28, 29, 30, 33, 36, 39, 42, 46, 49, 53, 56, 60, 64, 68, 72, 77, 81, 86,
        90, 95, 100, 105, 110, 116, 121, 127, 132, 138, 144, 150, 156, 163, 169,
        176, 182, 189, 196, 203, 210, 218, 225, 233, 240, 248, 255,
    )

    private fun checkRule() {
        if (button.isSingle()) {
            // Logger.info { "BUTTON IS SINGLE" }
            processSingle()
        } else if (button.isHolded()) {
            processHold()
        }
    }

    private fun processHold() {
        if (ledSwitch != null) {
            ledSwitch.value = true
            led.bright = 0
        }
        Rule.addTask {
            delay(startDelay * calculateLedValue(curLevel) / led.maxValue)
            while (button.isHold()) {
                led.bright = calculateLedValue(brights[curLevel])
                curLevel += speed
                curLevel = curLevel.coerceIn(0, brights.size - 1)

                delay(50L)
            }
            speed *= -1
            // Logger.info {
            //     "button is release " +
            //         "speed is $speed"
            // }
        }
    }

    private fun processSingle() {
        if (ledSwitch != null) {
            ledSwitch.value = !ledSwitch.value
        } else {
            led.changeState()
            speed = if (led.bright == Led.minValue) {
                1
            } else {
                -1
            }
            curLevel = if (led.bright == led.maxValue) {
                brights.lastIndex
            } else {
                0
            }
        }
    }

    private fun calculateLedValue(value: Int): Int {
        return (value.toDouble() / 255 * led.maxValue).roundToInt()
    }

    override fun deleteRule() {
        TopicBase.removeCallback(button::checkButton)
    }

    init {
        TopicBase.addCallback(listOf(buttonTopic), button::checkButton)
    }

    companion object {
        const val startDelay = 500L
    }
}
