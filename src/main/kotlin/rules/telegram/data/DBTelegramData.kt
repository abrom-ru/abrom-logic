package rules.telegram.data

import rules.RuleInfo
import rules.data.DBData

class DBTelegramData(info: RuleInfo) : TelegramData(info), DBData {
    init {
        loadFields()
    }
}
