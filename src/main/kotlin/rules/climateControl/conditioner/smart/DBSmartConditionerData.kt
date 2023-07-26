package rules.climateControl.conditioner.smart

import rules.RuleInfo
import rules.climateControl.conditioner.Conditioner
import rules.data.DBData

class DBSmartConditionerData(info: RuleInfo) : SmartConditionerData(info), DBData {
    init {
        loadFields()
    }

    override fun toRule(): Conditioner? {
        complete()
        return super.toRule()
    }
}
