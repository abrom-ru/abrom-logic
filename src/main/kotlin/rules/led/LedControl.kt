package rules.led

import models.RangeDevice
import models.Topic

class Led(topic: Topic, max: Int) {

    val maxValue: Int

    init {
        maxValue = max
    }

    var bright: Int by RangeDevice(topic)

    fun turnOn() {
        bright = maxValue
    }

    fun turnOff() {
        bright = minValue
    }

    fun setLight(value: Int) {
        bright = correctNewValue(value)
    }

    private fun correctNewValue(value: Int): Int {
        if (value > maxValue) {
            return maxValue
        }
        if (value < minValue) {
            return minValue
        }
        return value
    }

    fun changeState() {
        bright = if (bright != minValue) {
            minValue
        } else {
            maxValue
        }
    }

    companion object {
        const val minValue = 0
    }
}
