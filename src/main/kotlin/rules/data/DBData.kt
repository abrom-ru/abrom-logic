package rules.data

import RuleStorage

interface DBData : RuleData {
    fun loadFields() {
        RuleStorage.rules.getValueOfRule(info, fields)
    }
}
