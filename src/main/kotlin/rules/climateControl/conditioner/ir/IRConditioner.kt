package rules.climateControl.conditioner.ir

import models.ButtonDevice
import models.SwitchDevice
import models.Topic
import rules.RuleInfo
import rules.climateControl.conditioner.Conditioner
import rules.climateControl.conditioner.smart.SmartConditioner
import storage.TopicBase

class IRConditioner private constructor(
    info: RuleInfo,
    builder: Builder,
) : Conditioner(info, builder) {

    private val coolingChannels = List(5) { id -> getChannelById(builder, id + 1) }
    private val heatingChannels = List(5) { id -> getChannelById(builder, id + 7) }
    private var conditionerState: Boolean by SwitchDevice(builder.mqttState)
    private val offChannel = getChannelById(builder, 6)
    private var curChannel = offChannel

    /**
     * Turn on [channel] mode of conditioner.
     *
     * @param channel mode we want to be.
     */
    private fun send(channel: ButtonDevice) {
        if (channel != curChannel) {
            curChannel = channel.apply { value = true }
            conditionerState = getState()
        }
    }

    private fun controlState() {
        if (canBeOn) {
            controlCurMode()
            if (ruleState) {
                if (curMode == ConditionerMode.COOL) {
                    controlCoolMode()
                } else {
                    controlHeatMode()
                }
            } else {
                shutdown()
            }
        } else {
            shutdown()
        }
    }

    private fun controlHeatMode() {
        if (roomTemp == null || useRoomTemp == null || !useRoomTemp.value) {
            send(tempGroup(prefTemp))
        } else {
            if (prefTemp > roomTemp.value) {
                send(tempGroup(prefTemp))
            } else if (prefTemp < roomTemp.value - SmartConditioner.DELTA) {
                shutdown()
            }
        }
    }

    private fun controlCoolMode() {
        if (roomTemp == null || useRoomTemp == null || !useRoomTemp.value) {
            send(tempGroup(prefTemp))
        } else {
            if (prefTemp > roomTemp.value) {
                shutdown()
            } else if (prefTemp < roomTemp.value - SmartConditioner.DELTA) {
                send(tempGroup(prefTemp))
            }
        }
    }

    override fun checkState() {
        if (System.currentTimeMillis() - timer > INIT_DELAY * 1000) {
            super.checkState()
            controlState()
        } else {
            shutdown()
        }
    }

    /**
     * Shutdown conditioner
     *
     */
    override fun shutdown() {
        conditionerState = false
        send(offChannel)
    }

    /**
     * get state of conditioner
     *
     * @return true if conditoiner is on and false else
     */
    override fun getState(): Boolean {
        return curChannel != offChannel
    }

    private fun getChannelById(builder: Builder, id: Int): ButtonDevice {
        return ButtonDevice(builder.device.copy(channel = "Play from ROM$id"))
    }

    /**
     * Get temperature mode of [temp].
     *
     * @param temp temperature we want to get.
     * @return mode of [temp] we want to turn on.
     */

    private fun tempGroup(temp: Int): ButtonDevice {
        return if (curMode == ConditionerMode.COOL) {
            when (temp) {
                in 16..19 -> coolingChannels[0]
                in 20..22 -> coolingChannels[1]
                in 23..25 -> coolingChannels[2]
                in 26..28 -> coolingChannels[3]
                in 29..30 -> coolingChannels[4]
                else -> offChannel
            }
        } else {
            when (temp) {
                in 18..20 -> heatingChannels[0]
                in 21..23 -> heatingChannels[1]
                in 24..26 -> heatingChannels[2]
                in 27..29 -> heatingChannels[3]
                in 30..31 -> heatingChannels[4]
                else -> offChannel
            }
        }
    }

    override fun deleteRule() {
        TopicBase.removeCallback(::checkState)
        super.deleteRule()
    }

    init {
        TopicBase.addCallback(
            listOf(
                builder.winterMode,
                builder.nobodyAtHome,
                builder.outsideTemp,
                builder.prefTemp,
                builder.mqttState,
                builder.roomTemp,
                builder.mode,
                builder.useRoomTemp,
                builder.conditionerTemp,
                builder.ruleState,
            ),
            ::checkState,
        )
    }

    class Builder : Conditioner.Builder<Builder>() {

        var device: Topic = Topic.empty

        override fun self(): Builder {
            return this
        }

        fun setDevice(topic: Topic): Builder {
            device = topic
            return self()
        }

        override fun build(): Conditioner? {
            if (listOf(
                    this.device,
                    this.nobodyAtHome,
                    this.mqttState,
                    this.winterMode,
                    this.prefTemp,
                ).contains(Topic.empty)
            ) {
                return null
            }
            return IRConditioner(info, this)
        }
    }
}
