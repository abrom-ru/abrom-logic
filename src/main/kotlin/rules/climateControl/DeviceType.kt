package rules.climateControl

import models.js.CellType

/**
 * DeviceType class documentation.
 *
 * This class contain types of mqtt devices
 *
 * @author Abdulkhakov Artur arturabdulkhakov1@gmail.com
 */
enum class DeviceType(
    val jsName: String,
    val CellType: CellType,
    val isGlobal: Boolean = false,
    val min: Int? = null,
    val max: Int? = null,
) {
    WINTER_MODE("winter mode", CellType.SWITCH, true),
    COMFORT_FLOOR_TEMP("comfort floor temp", CellType.RANGE, false, 15, 30),
    COMFORT_FLOOR_MODE("comfort floor mode", CellType.SWITCH, false),
    PREF_TEMP("pref temp", CellType.RANGE, false, 15, 30),
    CONDITIONER_SHUTDOWN("conditioner shutdown", CellType.SWITCH),
    USE_ROOM_TEMP("use room temp", CellType.SWITCH),
    NOBODY_AT_HOME("nobody at home", CellType.SWITCH, true),
    CONDITIONER_STATE("conditioner state", CellType.SWITCH),
    CUR_TEMP("current temp", CellType.TEMP),
    CONDITIONER_MODE("conditioner mode", CellType.RANGE, min = 0, max = 1),
    RULE_STATE("Rule state", CellType.SWITCH),
    CONDITIONER_TEMP("conditioner temp", CellType.TEMP),
    ROOM_TEMP("room temp", CellType.TEMP),
}
