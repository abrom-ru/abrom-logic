package rules.climateControl.conditioner

import models.RangeDevice
import models.SensorDevice
import models.SwitchDevice
import models.Topic
import models.js.Cell
import models.js.VirtualDevice
import rules.Rule
import rules.RuleInfo
import rules.climateControl.DeviceType

abstract class Conditioner(info: RuleInfo, builder: Builder<*>) : Rule {

    val ruleName: String

    init {
        timer = System.currentTimeMillis()
        val list = mutableListOf(
            DeviceType.PREF_TEMP,
            DeviceType.CONDITIONER_MODE,
            DeviceType.CONDITIONER_STATE,
            DeviceType.RULE_STATE,
        )
        if (builder.roomTemp != Topic.empty) list.add(DeviceType.USE_ROOM_TEMP)
        if (builder.displayedConditionerTemp != Topic.empty) list.add(DeviceType.CONDITIONER_TEMP)
        if (builder.displayedRoomTemp != Topic.empty) list.add(DeviceType.ROOM_TEMP)
        val device = VirtualDevice(
            "${info}_control",
            "$info conditioner",
            list.map { Cell(it) }.toMutableList(),
        )
        Rule.builder.add(
            device,
        )
        ruleName = device.deviceName
    }

    protected var curMode = ConditionerMode.COOL

    protected val roomTemp = if (builder.roomTemp != Topic.empty) SensorDevice(builder.roomTemp) else null
    protected val conditionerTemp =
        if (builder.conditionerTemp != Topic.empty) SensorDevice(builder.conditionerTemp) else null
    protected val useRoomTemp = if (builder.useRoomTemp != Topic.empty) SwitchDevice(builder.useRoomTemp) else null

    protected val displayedRoomTemp =
        if (builder.displayedRoomTemp != Topic.empty) SensorDevice(builder.displayedRoomTemp) else null
    protected val displayedConditonerTemp =
        if (builder.displayedConditionerTemp != Topic.empty) SensorDevice(builder.displayedConditionerTemp) else null

    fun controlCurMode() {
        curMode = when (mode) {
            0 -> ConditionerMode.COOL
            1 -> ConditionerMode.HEAT
            else -> ConditionerMode.COOL
        }
    }

    /**
     * Shutdown conditioner
     *
     */
    abstract fun shutdown()

    /**
     * get state of conditioner
     *
     * @return true if conditoiner is on and false else
     */
    abstract fun getState(): Boolean

    companion object {
        private const val minOutsideTemp = 10
        const val INIT_DELAY = 20

        var timer: Long = System.currentTimeMillis()
    }

    private val outsideTemp = if (builder.outsideTemp != Topic.empty) SensorDevice(builder.outsideTemp) else null
    private val winterMode: Boolean by SwitchDevice(builder.winterMode)
    private val nobodyAtHome: Boolean by SwitchDevice(builder.nobodyAtHome)
    protected val prefTemp: Int by RangeDevice(builder.prefTemp)
    protected val mode by RangeDevice(builder.mode)
    protected val ruleState by SwitchDevice(builder.ruleState)

    /**
     * can be conditioner turned on status.
     */
    val canBeOn: Boolean
        get() {
            return (!winterMode || outsideTemp == null || (winterMode && outsideTemp.value > minOutsideTemp)) && !nobodyAtHome && ruleState
        }

    abstract class Builder<T : Builder<T>> {
        lateinit var info: RuleInfo
        var outsideTemp: Topic = Topic.empty
        var winterMode: Topic = Topic.empty
        var nobodyAtHome: Topic = Topic.empty
        var mqttState: Topic = Topic.empty
        var prefTemp: Topic = Topic.empty
        var mode: Topic = Topic.empty
        var roomTemp: Topic = Topic.empty
        var useRoomTemp: Topic = Topic.empty
        var ruleState: Topic = Topic.empty
        var conditionerTemp: Topic = Topic.empty
        var displayedConditionerTemp: Topic = Topic.empty
        var displayedRoomTemp: Topic = Topic.empty

        protected abstract fun self(): T
        abstract fun build(): Conditioner?

        fun setOutsideTemp(topic: Topic): T {
            outsideTemp = topic
            return self()
        }

        fun setRuleInfo(ruleInfo: RuleInfo): T {
            info = ruleInfo
            return self()
        }

        fun setMqttState(topic: Topic): T {
            mqttState = topic
            return self()
        }

        fun setWinterMode(topic: Topic): T {
            winterMode = topic
            return self()
        }

        fun setNobodyAtHome(topic: Topic): T {
            nobodyAtHome = topic
            return self()
        }

        fun setMode(topic: Topic): T {
            mode = topic
            return self()
        }

        fun setPrefTemp(topic: Topic): T {
            prefTemp = topic
            return self()
        }

        fun setRoomTemp(topic: Topic): T {
            roomTemp = topic
            return self()
        }

        fun setRuleState(topic: Topic): T {
            ruleState = topic
            return self()
        }
    }

    protected open fun checkState() {
        if (conditionerTemp != null) {
            displayedConditonerTemp?.value = conditionerTemp.value
        }
        if (roomTemp != null) {
            displayedRoomTemp?.value = roomTemp.value
        }
        if (!canBeOn) {
            shutdown()
        }
    }

    override fun deleteRule() {
        Rule.builder.delete(ruleName)
    }

    enum class ConditionerMode {
        HEAT, COOL
    }
}
