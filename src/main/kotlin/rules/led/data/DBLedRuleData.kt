package rules.led.data

import rules.RuleInfo
import rules.data.DBData

class DBLedRuleData(info: RuleInfo) : LedRuleData(info), DBData {
    init {
        loadFields()
    }
}
