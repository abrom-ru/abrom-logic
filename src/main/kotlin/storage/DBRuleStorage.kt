package storage

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import rules.RuleInfo
import rules.RuleTypes
import rules.data.FieldData
import java.lang.Thread.sleep

/**
 * Класс для общения с базой, лучше переписать на Exposed DAO
 */
class DBRuleStorage(private val url: String, private val driver: String) {

    fun init() {
        Database.connect(url, driver)
        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(RuleData)
            SchemaUtils.create(Rule)
            SchemaUtils.create(RuleType)
            SchemaUtils.create(FieldsType)
            commit()
        }
        sleep(1000) // Initializing database
    }

    fun deleteRule(rule: RuleInfo) {
        transaction {
            addLogger(StdOutSqlLogger)
            val ruleId = getRuleOrNull(rule) ?: return@transaction

            RuleData.deleteWhere { RuleData.rule eq ruleId }
            Rule.deleteWhere { Rule.id eq ruleId }
            commit()
        }
    }

    fun addRule(name: RuleInfo, fields: Map<FieldData, String>) {
        transaction {
            insertRuleInfo(name)
            insertFields(name, fields)
            commit()
        }
    }

    private fun createOrGetRuleType(type: RuleTypes): EntityID<Int> {
        return transaction {
            RuleType.select { RuleType.type eq type.dbName }
                .singleOrNull()
                ?.get(RuleType.id)
                ?: RuleType.insertAndGetId {
                    it[RuleType.type] = type.dbName
                }.also { commit() }
        }
    }

    private fun createOrGetFieldData(field: FieldData): EntityID<Int> {
        return transaction {
            FieldsType.select { FieldsType.type eq field.dbName }
                .singleOrNull()
                ?.get(FieldsType.id)
                ?: (
                    FieldsType.insertAndGetId {
                        it[type] = field.dbName
                    }
                    ).also { commit() }
        }
    }

    private fun getRuleOrNull(info: RuleInfo): EntityID<Int>? {
        return transaction {
            val typeId = createOrGetRuleType(info.type)
            Rule.select {
                Rule.name eq info.name and (Rule.type eq typeId)
            }.firstOrNull()?.get(Rule.id).also { commit() }
        }
    }

    private fun insertFields(ruleInfo: RuleInfo, fields: Map<FieldData, String>) {
        transaction {
            getRuleOrNull(ruleInfo) ?: return@transaction
            fields.forEach { (data, value) ->
                createValueOrUpdate(ruleInfo, data, value)
            }
            commit()
        }
    }

    private fun createValueOrUpdate(info: RuleInfo, field: FieldData, aValue: String) {
        transaction {
            val ruleId = getRuleOrNull(info)
            val fieldId = createOrGetFieldData(field)
            if (ruleId != null) {
                val ruleData = RuleData.select { (RuleData.rule eq ruleId) and (RuleData.fieldType eq fieldId) }
                    .firstOrNull()
                if (ruleData != null) {
                    RuleData.update({ RuleData.id eq ruleData[RuleData.id] }) {
                        it[value] = aValue
                    }
                } else {
                    RuleData.insert {
                        it[rule] = ruleId
                        it[fieldType] = fieldId
                        it[value] = aValue
                    }
                }
            }
            commit()
        }
    }

    private fun getValueOrNull(info: RuleInfo, field: FieldData): String? {
        return transaction {
            val rule = getRuleOrNull(info)
            val type = createOrGetFieldData(field)
            if (rule == null) return@transaction null
            RuleData.select {
                RuleData.rule eq rule and (RuleData.fieldType eq type)
            }.firstOrNull()?.getOrNull(RuleData.value).also { commit() }
        }
    }

    private fun insertRuleInfo(info: RuleInfo) {
        transaction {
            val ruleId = createOrGetRuleType(info.type)
            Rule.insertIgnore {
                it[name] = info.name
                it[type] = ruleId
            }
        }
    }

    fun getRules(): List<RuleInfo> {
        return transaction {
            Rule.selectAll().mapNotNull {
                val ruleType = valueOfRuleType(
                    RuleType.select { RuleType.id eq it[Rule.type] }
                        .singleOrNull()?.get(RuleType.type) ?: return@mapNotNull null,
                ) ?: return@mapNotNull null

                RuleInfo(it[Rule.name], ruleType).also { commit() }
            }
        }
    }

    private fun valueOfRuleType(dbName: String?): RuleTypes? {
        return RuleTypes.values().firstOrNull { it.name == dbName }
    }

    fun getValueOfRule(info: RuleInfo, fields: MutableMap<FieldData, String>) {
        transaction {
            fields.keys.forEach { data ->
                createOrGetFieldData(data)
                val ruleId = getRuleOrNull(info)
                if (ruleId != null) {
                    val value = getValueOrNull(info, data)
                    if (value != null) {
                        fields[data] = value
                    }
                }
            }
            commit()
        }
    }
}

object RuleData : IntIdTable() {
    val rule = reference("rule_id", Rule)
    val fieldType = reference("field_id", FieldsType)
    val value = varchar("value", 500)

    init {
        uniqueIndex(rule, fieldType)
    }
}

object Rule : IntIdTable() {
    val name = varchar("name", 50)
    val type = reference("type_id", RuleType)

    init {
        uniqueIndex("rules_unique_name_type", name, type)
    }
}

object FieldsType : IntIdTable() {
    val type = varchar("type", 50).uniqueIndex()
}

object RuleType : IntIdTable() {
    val type = varchar("type", 50).uniqueIndex()
}
