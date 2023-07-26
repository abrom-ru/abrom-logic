package rules.climateControl

import models.RangeDevice
import models.SensorDevice
import models.SwitchDevice
import models.TaskManager
import models.Topic
import models.js.Cell
import models.js.CellType
import models.js.VirtualDevice
import rules.Rule
import rules.RuleInfo
import storage.TopicBase
import javax.management.timer.Timer

class FloorController(
    relays: List<Topic>,
    floorTemp: Topic,
    roomTemp: Topic,
    inverted: Boolean,
    val isWater: Boolean,
    ruleInfo: RuleInfo,
) : Rule {

    private val fullTime = 30

    private val taskManager = TaskManager()

    private val ruleName = ruleInfo.toString() + "_control"

    private val state = SwitchDevice(ruleName, "state")

    private val timerModeField = "timer mode"

    private val timeSelectField = "on time | off time"

    private val displayedTemp = "current room temp"

    private val maxFloorTempCell = "max floor temp"

    private val heatModeCell = "heat mode"

    private val currentDisplayedRoomTemp: SensorDevice? =
        if (roomTemp != Topic.empty) SensorDevice(ruleName, displayedTemp) else null

    private val currentDisplayedFloorRoom: SensorDevice? =
        if (floorTemp != Topic.empty) SensorDevice(ruleName, "floor temp") else null

    private val roomTemp: SensorDevice? = if (roomTemp != Topic.empty) SensorDevice(roomTemp) else null

    private val floorTemp: SensorDevice? = if (floorTemp != Topic.empty) SensorDevice(floorTemp) else null

    private val minFloorTemp: RangeDevice? =
        if (floorTemp != Topic.empty) RangeDevice(ruleName, "min floor temp") else null

    private val prefRoomTemp: RangeDevice? = if (roomTemp != Topic.empty) {
        RangeDevice(
            ruleName,
            "pref room temp",
        )
    } else {
        null
    }

    private val prefFloorTemp: RangeDevice? =
        if (floorTemp != Topic.empty) RangeDevice(ruleName, "pref floor temp") else null

    private val heatMode =
        if (prefRoomTemp != null && prefFloorTemp != null) SwitchDevice(ruleName, heatModeCell) else null

    private val maxFloorTemp: RangeDevice? =
        if (floorTemp != Topic.empty) RangeDevice(ruleName, maxFloorTempCell) else null

    private val relays = relays.map { SwitchDevice(it, inverted) }

    init {
        val device = VirtualDevice(ruleName, "Отопление $ruleInfo")
        device.addCell(Cell(timerModeField, CellType.SWITCH))
        device.addCell(Cell(timeSelectField, CellType.RANGE, min = 0, max = 25))
        val callbackTopics = mutableListOf<Topic>()
        if (currentDisplayedRoomTemp != null) {
            device.addCell(Cell(currentDisplayedRoomTemp.topic.channel, CellType.TEMP))
        }
        if (currentDisplayedFloorRoom != null) {
            device.addCell(Cell(currentDisplayedFloorRoom.topic.channel, CellType.TEMP))
        }

        if (prefRoomTemp != null) {
            device.addCell(Cell(prefRoomTemp.topic.channel, CellType.RANGE, min = 15, max = 40))
            callbackTopics.add(prefRoomTemp.topic)
        }

        if (prefFloorTemp != null) {
            device.addCell(Cell(prefFloorTemp.topic.channel, CellType.RANGE, min = 15, max = 40))
            callbackTopics.add(prefFloorTemp.topic)
        }

        if (minFloorTemp != null) {
            device.addCell(Cell(minFloorTemp.topic.channel, CellType.RANGE, min = 15, max = 40))
            callbackTopics.add(minFloorTemp.topic)
        }

        if (heatMode != null) {
            device.addCell(Cell(heatMode.topic.channel, CellType.SWITCH))
            callbackTopics.add(heatMode.topic)
        }

        if (maxFloorTemp != null) {
            device.addCell(Cell(maxFloorTemp.topic.channel, CellType.RANGE, min = 15, max = 40))
            callbackTopics.add(maxFloorTemp.topic)
        }

        if (floorTemp != Topic.empty) {
            callbackTopics.add(floorTemp)
        }

        if (roomTemp != Topic.empty) {
            callbackTopics.add(roomTemp)
        }

        device.addCell(Cell(state.topic.channel, CellType.SWITCH))

        callbackTopics.add(state.topic)

        Rule.builder.add(device)

        Rule.builder.save()

        TopicBase.addCallback(
            callbackTopics,
            ::checkRule,
        )

        TopicBase.addCallback(
            mutableListOf(Topic(ruleName, timerModeField), Topic(ruleName, timeSelectField)),
            ::onTimerMode,
        )
    }

    private var onTime: Int by RangeDevice(Topic(ruleName, timeSelectField))

    var timerMode: Boolean by SwitchDevice(Topic(ruleName, timerModeField))
    private val offTime: Int
        get() {
            return fullTime - onTime
        }

    private var relayState: Boolean = false
        set(value) {
            relays.forEach {
                it.value = value
            }
            field = value
        }

    private fun onTimerMode() {
        if (timerMode) {
            changeState()
        } else {
            relayState = false
        }
        checkRule()
    }

    @Synchronized
    fun checkRule() {
        if (floorTemp != null) {
            currentDisplayedFloorRoom?.value = floorTemp.value
        }
        if (roomTemp != null) {
            currentDisplayedRoomTemp?.value = roomTemp.value
        }

        if (timerMode && prefRoomTemp != null && roomTemp != null && roomTemp.value > prefRoomTemp.value) {
            relayState = false
        }

        if (!state.value) {
            timerMode = false
            relayState = false
        } else {
            if ((floorTemp != null && maxFloorTemp != null) && floorTemp.value >= maxFloorTemp.value) {
                relayState = false
                timerMode = false
            }
            if (!timerMode && (prefFloorTemp != null || prefRoomTemp != null) && (isWater || floorTemp != null)) {
                if ((heatMode != null && !heatMode.value) || prefRoomTemp == null) {
                    if (floorTemp!!.value >= prefFloorTemp!!.value) {
                        relayState = false
                    } else if (floorTemp.value <= prefFloorTemp.value - TEMP_DELTA) {
                        relayState = true
                    }
                } else {
                    if (floorTemp != null && prefFloorTemp != null && minFloorTemp != null) {
                        if (floorTemp.value < minFloorTemp.value) {
                            relayState = true
                        } else if (floorTemp.value > minFloorTemp.value + TEMP_DELTA) {
                            relayState = getStateByRoomTemp()
                        }
                    } else {
                        relayState = getStateByRoomTemp()
                    }
                }
            }
        }
    }

    private fun getStateByRoomTemp(): Boolean {
        if (roomTemp!!.value > prefRoomTemp!!.value) {
            return false
        }
        if (roomTemp.value < prefRoomTemp.value - TEMP_DELTA) {
            return true
        }
        return relayState
    }

    override fun deleteRule() {
        Rule.builder.delete(ruleName)
        TopicBase.removeCallback(::checkRule)
        TopicBase.removeCallback(::onTimerMode)
    }

    @Synchronized
    private fun changeState() {
        relayState = if (timerMode && state.value) {
            if (relayState) {
                taskManager.add(Timer.ONE_MINUTE * offTime) { changeState() }
            } else {
                taskManager.add(Timer.ONE_MINUTE * onTime) { changeState() }
            }
            if (prefRoomTemp != null && roomTemp != null && roomTemp.value > prefRoomTemp.value) {
                false
            } else {
                !relayState
            }
        } else {
            false
        }
    }

    companion object {
        private const val TEMP_DELTA = 1
    }
}
