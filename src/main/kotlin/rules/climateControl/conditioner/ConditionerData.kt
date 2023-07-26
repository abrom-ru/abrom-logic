package rules.climateControl.conditioner

import rules.RuleInfo
import rules.climateControl.HomeBuilder
import rules.data.RuleData

abstract class ConditionerData(override val info: RuleInfo) : RuleData {
    abstract val builder: Conditioner.Builder<*>

    abstract fun complete(): Boolean
    override fun toRule(): Conditioner? {
        builder.info = info
        HomeBuilder.addConditionerDevices(info, builder)
        if (!complete()) return null
        return builder.build()
    }
}
