package rules.data

import models.js.CellType

interface FieldData {
    val jsName: String
    val label: String
    val dbName: String
    val type: CellType
    val dataType: DataType
}

enum class DataType {
    INT, DOUBLE, BOOLEAN, STRING, TOPIC, TOPIC_ARRAY
}
