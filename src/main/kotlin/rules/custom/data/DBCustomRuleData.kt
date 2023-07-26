package rules.custom.data

import rules.RuleInfo
import rules.data.DBData

class DBCustomRuleData(info: RuleInfo) : CustomRuleData(info), DBData {
    init {
        loadFields()
    }
}
