package rules.climateControl.conditioner.smart

import models.RangeDevice
import models.SwitchDevice
import models.Topic
import rules.RuleInfo
import rules.climateControl.conditioner.Conditioner
import storage.TopicBase

class SmartConditioner(info: RuleInfo, builder: Builder) : Conditioner(info, builder) {

    private var stateSwitch by SwitchDevice(builder.stateSwitch)
    private var heatSwitch by SwitchDevice(builder.heatSwitch)
    private var tempSwitcher by RangeDevice(builder.tempSwitcher)
    private var conditionerState by SwitchDevice(builder.mqttState)

    private fun controlTemp() {
        controlCurMode()
        tempSwitcher = prefTemp
        if (canBeOn) {
            when (curMode) {
                ConditionerMode.HEAT -> controlHeatMode()
                ConditionerMode.COOL -> controlCoolMode()
            }
        } else {
            shutdown()
        }
    }

    private fun controlHeatMode() {
        heatSwitch = true
        if (roomTemp == null || useRoomTemp == null || !useRoomTemp.value) {
            conditionerState = true
            stateSwitch = true
            tempSwitcher = prefTemp
        } else {
            tempSwitcher = prefTemp
            if (prefTemp > roomTemp.value) {
                stateSwitch = true
                conditionerState = true
            } else if (
                prefTemp <= roomTemp.value - DELTA
            ) {
                conditionerState = false
                stateSwitch = false
            }
        }
    }

    private fun controlCoolMode() {
        heatSwitch = false
        if (roomTemp == null || useRoomTemp == null || !useRoomTemp.value) {
            conditionerState = true
            stateSwitch = true
            tempSwitcher = prefTemp
        } else {
            tempSwitcher = prefTemp
            if (prefTemp > roomTemp.value) {
                stateSwitch = false
                conditionerState = false
            } else if (prefTemp <= roomTemp.value - DELTA) {
                stateSwitch = true
                conditionerState = true
            }
        }
    }

    class Builder : Conditioner.Builder<Builder>() {
        var tempSwitcher: Topic = Topic.empty
        var heatSwitch: Topic = Topic.empty
        var stateSwitch: Topic = Topic.empty
        override fun self(): Builder {
            return this
        }

        fun setStateSwitch(topic: Topic): Builder {
            stateSwitch = topic
            return this
        }

        fun setHeatSwitch(topic: Topic): Builder {
            heatSwitch = topic
            return this
        }

        fun setTempSwitcher(topic: Topic): Builder {
            tempSwitcher = topic
            return this
        }

        override fun build(): Conditioner {
            return SmartConditioner(info, this)
        }
    }

    /**
     * Shutdown conditioner
     *
     */
    override fun shutdown() {
        conditionerState = false
        stateSwitch = false
    }

    /**
     * get state of conditioner
     *
     * @return true if conditoiner is on and false else
     */
    override fun getState(): Boolean {
        return stateSwitch
    }

    override fun deleteRule() {
        super.deleteRule()
        TopicBase.removeCallback(::checkState)
    }

    override fun checkState() {
        super.checkState()
        controlTemp()
    }

    init {
        TopicBase.addCallback(
            listOf(
                builder.useRoomTemp,
                builder.roomTemp,
                builder.conditionerTemp,
                builder.mode,
                builder.ruleState,
                builder.winterMode,
                builder.nobodyAtHome,
                builder.outsideTemp,
                builder.prefTemp,
            ),
            ::checkState,
        )
    }

    companion object {
        const val DELTA = 1
    }
}
