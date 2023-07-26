package rules.climateControl.data

import rules.RuleInfo
import rules.data.DBData

class DBFloorControllerData(info: RuleInfo) : FloorControllerData(info), DBData {
    init {
        loadFields()
    }
}
