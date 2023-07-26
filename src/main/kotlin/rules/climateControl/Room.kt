package rules.climateControl

import models.SensorDevice
import models.SwitchDevice
import models.TextDevice
import models.Topic
import rules.Rule
import rules.RuleInfo
import storage.TopicBase

const val STEP = 1.0

class Room(
    ruleInfo: RuleInfo,
    floorRelay: List<Topic>,
    heatRelay: List<Topic>,
    curFloorTemp: Topic,
    curTemp: Topic,
    curJsTemp: Topic,
    minFloorTemp: Topic,
    private val maxFloorTemp: Int,
    prefTemp: Topic,
    comfortFloor: Topic,
    isFloorInverted: Boolean,
    isHeatInverted: Boolean,
) : Rule {

    /**
     * State of room's heat relay
     */
    private val floorRelay = floorRelay.map { SwitchDevice(it, isFloorInverted) }
    private val heatRelay = heatRelay.map { SwitchDevice(it, isHeatInverted) }
    private var minFloorTemp: Double by SensorDevice(minFloorTemp)
    private var curJsTemp by TextDevice(curJsTemp)

    private var heatRelayState: Boolean = false
        set(value) {
            heatRelay.forEach {
                it.value = value
                field = value
            }
        }
    private var floorRelayState: Boolean = false
        set(value) {
            floorRelay.forEach {
                it.value = value

                field = value
            }
        }

    private val floorController: FloorController = TODO()

    /**
     * Comfort floor state value, if not null floor temperature can't be less than [minFloorTemp]
     */
    private val comfortFloor: Boolean by SwitchDevice(comfortFloor)

    /**
     * Preferred  temperature, when changed call [climateControl]
     */
    private var prefTemp: Double by SensorDevice(prefTemp)

    /**
     * Current  floor temperature
     */
    private val curFloorTemp: Double by SensorDevice(curFloorTemp)

    /**
     * Current room temperature
     */
    private val curTemp: Double by SensorDevice(curTemp)

    /**
     * call all control's fun
     */
    private fun climateControl() {
        floorControl()
        termControl()
    }

    /**
     * control floor temperature
     *
     */
    private fun floorControl() {
        if (floorController.timerMode == false) {
            floorRelayState = when (curFloorTemp) {
                in Double.NEGATIVE_INFINITY..(maxFloorTemp - STEP * 5) -> getFloorState()
                in (maxFloorTemp - STEP * 5)..(maxFloorTemp.toDouble()) -> floorRelayState
                in (maxFloorTemp.toDouble())..(Double.POSITIVE_INFINITY) -> false
                else -> {
                    getFloorState()
                }
            }
        }
    }

    /**
     * Get floor state
     *
     * @return true if next state is on and false else
     */
    private fun getFloorState(): Boolean {
        return if (comfortFloor) {
            when (curFloorTemp) {
                in Double.NEGATIVE_INFINITY..(minFloorTemp - STEP) -> true
                in (minFloorTemp - STEP)..(STEP) -> floorRelayState
                else -> tempRegulation(curTemp, prefTemp, floorRelayState)
            }
        } else {
            tempRegulation(curTemp, prefTemp, floorRelayState)
        }
    }

    /**
     * control room temperature
     *
     */
    private fun termControl() {
        heatRelayState = tempRegulation(curTemp, prefTemp, heatRelayState)
    }

    /**
     * Control temperature in [prefTemp] - [STEP], [prefTemp] range
     *
     * @param tempSensor current temperature
     * @param prefTemp preferred temperature
     * @param curState current relay state
     * @return state of relay needed to control temperature
     */
    private fun tempRegulation(tempSensor: Double, prefTemp: Double, curState: Boolean): Boolean {
        return if (tempSensor > prefTemp) {
            false
        } else if (tempSensor <= prefTemp - STEP) {
            true
        } else {
            curState
        }
    }

    /**
     * Check state
     *
     */
    private fun checkRule() {
        curJsTemp = curTemp.toString()
        climateControl()
    }

    override fun deleteRule() {
        TopicBase.removeCallback(::checkRule)
        // builder.delete(ruleInfo.toString())
        floorController.deleteRule()
    }

    fun setPrefFromHttpTemp(temp: Double) {
        prefTemp = temp
        checkRule()
    }

    fun info(): Map<String, String> {
        return mapOf("temp" to prefTemp.toString())
    }

    init {
        val topics: List<Topic> =
            (floorRelay + curTemp + curFloorTemp + heatRelay + curJsTemp + minFloorTemp + prefTemp + comfortFloor).filter { it != Topic.empty }

        TopicBase.addCallback(topics, ::checkRule)
        heatRelayState = false
        floorRelayState = false
        floorController = TODO() /*if (floorRelay.isNotEmpty()) {
            FloorController(this.floorRelay, ruleInfo.toString())
        } else {
            null
        }*/
    }
}
