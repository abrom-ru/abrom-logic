package models.js

import rules.climateControl.DeviceType

data class Cell(
    val name: String,
    val cellType: CellType = CellType.TEXT,
    val readOnly: Boolean = false,
    val min: Int? = null,
    val max: Int? = null,
    var value: String? = null,
    var order: Int? = null,
) {
    override fun toString(): String {
        return " '$name': {\n" +
            "type: \"${cellType.textType}\",\n" +
            (if (cellType.defaultValue != null) "value: ${if (value == null) cellType.defaultValue else "\"$value\""},\n" else "") +
            (if (min != null) "min: $min,\n" else "") +
            (if (max != null) "max: $max,\n" else "") +
            (if (order != null) "order: $order, \n" else "") +
            "readonly: $readOnly\n" +
            "}"
    }

    constructor(device: DeviceType) : this(
        device.jsName,
        device.CellType,
        false,
        device.min,
        device.max,
    )
}
