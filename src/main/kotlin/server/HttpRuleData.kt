package server

import kotlinx.serialization.Serializable
import rules.RuleTypes

@Serializable
data class HttpRuleData(val name: String, val type: RuleTypes, val fields: Map<String, String>)
