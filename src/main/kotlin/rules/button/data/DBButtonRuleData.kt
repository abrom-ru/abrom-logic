package rules.button.data

import rules.RuleInfo
import rules.data.DBData

class DBButtonRuleData(info: RuleInfo) : ButtonRuleData(info), DBData {
    init {
        loadFields()
    }
}
