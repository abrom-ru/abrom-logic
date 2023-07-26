package rules.watering.data

import RuleStorage
import rules.RuleInfo
import rules.data.DBData

class DBWateringRuleData(override val info: RuleInfo) : WateringRuleData(info), DBData {
    init {
        RuleStorage.rules.getValueOfRule(info, fields)
    }
}
