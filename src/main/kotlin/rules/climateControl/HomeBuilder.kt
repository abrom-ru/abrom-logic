package rules.climateControl

import models.Topic
import models.js.Cell
import models.js.VirtualDevice
import rules.Rule
import rules.RuleInfo
import rules.climateControl.DeviceType.COMFORT_FLOOR_MODE
import rules.climateControl.DeviceType.COMFORT_FLOOR_TEMP
import rules.climateControl.DeviceType.CONDITIONER_STATE
import rules.climateControl.DeviceType.CUR_TEMP
import rules.climateControl.DeviceType.NOBODY_AT_HOME
import rules.climateControl.DeviceType.PREF_TEMP
import rules.climateControl.DeviceType.WINTER_MODE
import rules.climateControl.DeviceType.values
import rules.climateControl.conditioner.Conditioner
import rules.climateControl.data.RoomData

object HomeBuilder {

    init {
        createGlobalDevices()
    }

    private fun createGlobalDevices() {
        Rule.builder.add(VirtualDevice("home_control", "home", getGlobalDevices()))
    }

    private fun getGlobalDevices(): MutableList<Cell> {
        return values().filter { it.isGlobal }.map { Cell(it) }.toMutableList()
    }

    fun fillHomeDevices(roomData: RoomData) {
        createVirtualDevice(roomData)
        Rule.builder.add(roomData.getVirtualDevice())
    }

    private fun createVirtualDevice(data: RoomData) {
        addRoomDevices(data)
        addHomeDevices(data)
    }

    private fun addRoomDevices(data: RoomData) {
        data.prefTemp = Topic("${data.info.name}_room", PREF_TEMP.jsName)
        data.minFloorTemp = Topic("home_control", COMFORT_FLOOR_TEMP.jsName)
        data.curJsTemp = Topic("${data.info.name}_room", CUR_TEMP.jsName)
    }

    private fun addHomeDevices(data: RoomData) {
        data.comfortFloorMode = Topic("home_control/${COMFORT_FLOOR_MODE.jsName}")
    }

    fun addConditionerDevices(info: RuleInfo, builder: Conditioner.Builder<*>) {
        builder.mqttState = Topic("${info}_control", CONDITIONER_STATE.jsName)
        builder.nobodyAtHome = Topic("home_control", NOBODY_AT_HOME.jsName)
        builder.winterMode = Topic("home_control", WINTER_MODE.jsName)
        builder.prefTemp = Topic("${info}_control", PREF_TEMP.jsName)
        if (builder.roomTemp != Topic.empty) {
            builder.useRoomTemp = Topic("${info}_control", DeviceType.USE_ROOM_TEMP.jsName)
        }
        builder.mode = Topic("${info}_control", DeviceType.CONDITIONER_MODE.jsName)
        builder.ruleState = Topic("${info}_control", DeviceType.RULE_STATE.jsName)
        if (builder.conditionerTemp != Topic.empty) {
            builder.displayedConditionerTemp = Topic("${info}_control", DeviceType.CONDITIONER_TEMP.jsName)
        }
        if (builder.roomTemp != Topic.empty) {
            builder.displayedRoomTemp = Topic("${info}_control", DeviceType.ROOM_TEMP.jsName)
        }

        /*Rule.builder.add(
            VirtualDevice(
                "${info}_rule",
                "$info conditioner",
                listOf(PREF_TEMP, CONDITIONER_COOLING, CONDITIONER_HEAT, CONDITIONER_STATE).map { Cell(it) }
                    .toMutableList(),
            ),
        )*/
    }
}
