package rules.button

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import models.SwitchDevice
import models.Topic
import rules.Rule
import rules.button.TapType.CLICK
import rules.button.TapType.DOUBLE
import rules.button.TapType.HOLD
import rules.button.TapType.SINGLE
import rules.button.TapType.TRIPLE

@Suppress("KotlinConstantConditions")
class LightSwitch(topic: Topic, private val callback: () -> Unit) {
    private val state: Boolean by SwitchDevice(topic)

    init {
        Companion
    }

    fun checkButton() {
        launchRuleCheck()
    }

    private var ruleChecker: Job? = null

    private fun launchRuleCheck() {
        if (ruleChecker == null || ruleChecker?.isCompleted == true || ruleChecker?.isCancelled == true) {
            ruleChecker =
                Rule.addTask {
                    while (tick()) {
                        delay(checkDelay)
                    }
                }
        }
    }

    private fun tick(): Boolean {
        val curMillis = System.currentTimeMillis()
        buttonState = state
        // нажатие кнопки
        if (buttonState && !buttonFlag) {
            if (!buttonDebounce) {
                buttonDebounce = true
                buttonTimer = curMillis
            } else if (curMillis - buttonTimer >= debounce) {
                buttonFlag = true
                isPressFlag = true
                isOneFlag = true
                callback.invoke()
            }
        } else {
            buttonDebounce = false
        }
        // отпускание кнопки
        if (!buttonState && buttonFlag) {
            buttonFlag = false
            if (!holdFlag) buttonCounter++
            holdFlag = false
            isReleaseFlag = true
            buttonTimer = curMillis
            if (stepFlag) {
                lastCounter = 0
                buttonCounter = 0
                stepFlag = false
            }
            if (oneClickFlag) {
                oneClickFlag = false
                isOneFlag = true
            }
            callback.invoke()
        }
        // кнопка удерживается
        if (buttonFlag && buttonState && (curMillis - buttonTimer >= timeout) && !holdFlag) {
            holdFlag = true
            lastHoldCounter = buttonCounter
            isHoldedFlag = true
            stepFlag = true
            oneClickFlag = false
            buttonTimer = curMillis
            callback.invoke()
        }
        // обработка множественного нажатия
        if ((curMillis - buttonTimer >= clickTimeout) && (buttonCounter != 0) && !buttonState) {
            lastCounter = buttonCounter
            buttonCounter = 0
            counterFlag = true
            callback.invoke()
        }
        // сброс нажатий
        if (counterReset) {
            lastCounter = 0
            counterFlag = false
            counterReset = false
        }
        return (curMillis - buttonTimer) < checkTime
    }

    fun isPress() = if (isPressFlag) {
        // Logger.info {
        //     "BUTTON is PRESS"
        // }
        isPressFlag = false
        true
    } else {
        false
    }

    fun isRelease() = if (isReleaseFlag) {
        // Logger.info {
        //     "BUTTON is RELEASE"
        // }
        isReleaseFlag = false
        true
    } else {
        false
    }

    fun isClick() = if (isOneFlag) {
        // Logger.info {
        //     "BUTTON is CLICK"
        // }
        isOneFlag = false
        true
    } else {
        false
    }

    fun isHolded() = if (isHoldedFlag) {
        // Logger.info {
        //     "BUTTON is HOLDED"
        // }
        isHoldedFlag = false
        true
    } else {
        false
    }

    fun isHold() = stepFlag

    fun state() = buttonState

    fun isSingle() = if (counterFlag && lastCounter == 1) {
        // Logger.info {
        //     "BUTTON is SINGLE"
        // }
        counterReset = true
        true
    } else {
        false
    }

    fun isDouble() = if (counterFlag && lastCounter == 2) {
        // Logger.info {
        //     "BUTTON is DOUBLE"
        // }
        counterReset = true
        true
    } else {
        false
    }

    fun isTriple() = if (counterFlag && lastCounter == 3) {
        // Logger.info {
        //     "BUTTON is TRIPLE"
        // }
        counterReset = true
        true
    } else {
        false
    }

    fun hasClicks() = if (counterFlag) {
        counterReset = true
        true
    } else {
        false
    }

    fun getClick(): Int {
        // Logger.info {
        //     "BUTTON is HAS $lastCounter clicks"
        // }
        counterReset = true
        return lastCounter
    }

    fun getHoldClicks() = lastHoldCounter
    fun isStep(clicks: Int): Boolean {
        if (buttonCounter == clicks && stepFlag && (System.currentTimeMillis() - buttonTimer >= stepTimeout)) {
            buttonTimer = System.currentTimeMillis()
            return true
        }
        return false
    }

    fun checkTap(tapType: TapType): Boolean {
        return when (tapType) {
            SINGLE -> isSingle()
            DOUBLE -> isDouble()
            TRIPLE -> isTriple()
            HOLD -> isHolded()
            CLICK -> isClick()
        }
    }

    private var oneClickFlag = false
    private var isPressFlag = false
    private var isReleaseFlag = false
    private var isOneFlag = false
    private var isHoldedFlag = false
    private var holdFlag = false
    private var stepFlag = false
    private var counterFlag = false
    private var buttonTimer = 0L
    private var buttonCounter = 0
    private var lastCounter = 0
    private var lastHoldCounter = 0
    private var buttonState = false
    private var buttonFlag = false
    private var counterReset = false
    private var buttonDebounce = false

    private companion object {

        const val timeout = 500
        const val clickTimeout = 500
        const val stepTimeout = 300
        const val debounce = 20
        const val checkTime = 5000L
        const val checkDelay = 20L
    }
}

enum class TapType {
    SINGLE {
        override val label: String = "Одинарное"
    },
    DOUBLE {
        override val label: String = "Двойное"
    },
    TRIPLE {
        override val label: String = "Тройное"
    },
    HOLD {
        override val label: String = "Долгое"
    },
    CLICK {
        override val label: String = "Мгновенное"
    }, ;

    abstract val label: String
}
