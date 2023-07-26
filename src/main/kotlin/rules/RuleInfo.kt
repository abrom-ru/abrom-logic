package rules

import kotlinx.serialization.Serializable

/**
 * RuleInfo - информация о правиле
 *
 * @property name название правила
 * @property type тип правила
 */
@Serializable
data class RuleInfo(val name: String, val type: RuleTypes) {
    override fun toString(): String {
        return "${type}_$name"
    }
}
