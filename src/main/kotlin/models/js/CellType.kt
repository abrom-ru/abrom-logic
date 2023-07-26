package models.js

enum class CellType(val textType: String, val defaultValue: String? = null) {
    TEXT("text", "\"\""),
    PUSH_BUTTON("pushbutton"),
    RANGE("range", "0"),
    SWITCH("switch", "false"),
    TEMP("temperature", "0"),
}
