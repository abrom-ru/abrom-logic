package rules.climateControl.data

import rules.RuleInfo
import rules.data.DBData

class DBRoomData(info: RuleInfo) : RoomData(info), DBData {
    init {
        loadFields()
    }
}
