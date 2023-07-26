package rules.data

import RuleStorage
import rules.Rule
import rules.RuleInfo

interface RuleData {
    val info: RuleInfo

    val values: List<FieldData>
    fun toRule(): Rule?
    val fields: MutableMap<FieldData, String>

    fun getFields(types: List<FieldData>): MutableMap<FieldData, String> {
        return types.associateWithTo(mutableMapOf()) { "" }
    }

    fun save() {
        RuleStorage.rules.addRule(info, fields)
    }

    fun fillFields(data: Map<String, String>) {
        fields.putAll(getFields(values))
        fields.keys.forEach {
            fields[it] = data[it.toString()] ?: ""
        }
    }
}
