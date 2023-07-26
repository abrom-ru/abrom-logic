package rules.data

interface SlaveRuleData : RuleData {

    /**
     * Complete rule data with all needed fields
     *
     * @return true if data is complete and false else
     */
    fun complete(): Boolean
}
