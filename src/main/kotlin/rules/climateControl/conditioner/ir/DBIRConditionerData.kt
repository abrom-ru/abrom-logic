package rules.climateControl.conditioner.ir

import models.Topic
import models.toTopic
import rules.RuleInfo
import rules.climateControl.conditioner.Conditioner
import rules.data.DBData

class DBIRConditionerData(info: RuleInfo) : IRConditionerData(info), DBData {
    init {
        loadFields()
    }

    override fun toRule(): Conditioner? {
        builder.setDevice(Topic(fields[Fields.DEVICE]!!, ""))
        builder.setOutsideTemp(fields[Fields.OUTSIDE_TEMP].toTopic())
        return super.toRule()
    }
}
