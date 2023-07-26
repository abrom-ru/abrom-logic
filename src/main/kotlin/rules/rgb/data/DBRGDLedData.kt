package rules.rgb.data

import rules.RuleInfo
import rules.data.DBData

class DBRGDLedData(info: RuleInfo) : RGBLedData(info), DBData {
    init {
        loadFields()
    }
}
