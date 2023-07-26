package rules

import LaunchParams
import Logger
import RuleStorage
import models.ButtonDevice
import models.TextDevice
import models.Topic
import models.js.Cell
import models.js.CellType.PUSH_BUTTON
import models.js.JsConfig
import models.js.VirtualDevice
import rules.climateControl.HomeBuilder
import rules.data.FieldData
import rules.data.RuleData
import rules.data.SlaveRuleData
import storage.TopicBase

/**
 * Rule config класс для конфигурации правил
 *
 * @constructor
 *
 * @param path - путь для файла с конфигурацией wb-rules
 */
class RuleConfig private constructor(path: String) : JsConfig(
    VirtualDevice(
        MAIN_PAGE_TOPIC,
        MAIN_PAGE_TITLE,
        Cell(ADD_BUTTON_NAME, PUSH_BUTTON),
        Cell(NEW_RULE_TYPE),
        Cell(NEW_RULE_NAME),
        Cell(SAVE_BUTTON_NAME, PUSH_BUTTON),
        Cell(DELETE_BUTTON_NAME, PUSH_BUTTON),
    ),
    path,
) {
    init {
        HomeBuilder
        Rule
        TopicBase.addCallback(
            listOf(
                Topic(mainPage.deviceName, NEW_RULE_NAME),
                Topic(mainPage.deviceName, SAVE_BUTTON_NAME),
                Topic(mainPage.deviceName, ADD_BUTTON_NAME),
                Topic(mainPage.deviceName, DELETE_BUTTON_NAME),
                Topic(mainPage.deviceName, NEW_RULE_TYPE),
            ),
            ::checkButton,
        )
    }

    private var ruleNames: MutableList<RuleInfo> = mutableListOf()
    private val newRuleName: String by TextDevice(Topic(mainPage.deviceName, NEW_RULE_NAME))
    private val newRuleType: String by TextDevice(Topic(mainPage.deviceName, NEW_RULE_TYPE))
    private val saveButton: Boolean by ButtonDevice(Topic(mainPage.deviceName, SAVE_BUTTON_NAME))
    private val addButton: Boolean by ButtonDevice(Topic(mainPage.deviceName, ADD_BUTTON_NAME))
    private val deleteButton: Boolean by ButtonDevice(Topic(mainPage.deviceName, DELETE_BUTTON_NAME))
    private val unsavedData: MutableMap<RuleInfo, RuleData> = mutableMapOf()
    private val ruleData: MutableMap<RuleInfo, RuleData> = mutableMapOf()
    private val slaveData: MutableMap<RuleInfo, SlaveRuleData> = mutableMapOf()
    private val rule: HashMap<RuleInfo, Rule> = hashMapOf()

    /**
     * Проверка нажатия кнопок в окне конфигурации
     *
     */
    override fun checkButton() {
        if (saveButton) {
            saveRules()
            saveDB()
        }
        if (addButton) {
            addPage()
        }
        if (deleteButton) {
            val ruleInfo = getRuleInfo() ?: return
            deleteRule(ruleInfo)
        }
    }

    /**
     * Delete rule - удаление существующих правил
     *
     * @param ruleInfo информация о правиле которое хотим удалить
     */
    fun deleteRule(ruleInfo: RuleInfo) {
        rule[ruleInfo]?.deleteRule()
        rule.remove(ruleInfo)
        slaveData.remove(ruleInfo)
        ruleData.remove(ruleInfo)
        unsavedData.remove(ruleInfo)
        ruleNames.remove(ruleInfo)
        delete(getPageName(ruleInfo))
        RuleStorage.rules.deleteRule(ruleInfo)
    }

    /**
     * Create page создание окна настройки правила
     *
     * @param pageName информация о правиле которое хотим создать
     */
    override fun createPage(pageName: RuleInfo) {
        add(getPage(pageName))
    }

    /**
     * Добавить настройку текущего правила
     *
     */

    override fun addPage() {
        val info = getRuleInfo() ?: return
        if (newRuleName.isNotBlank() && !unsavedData.contains(info)) {
            unsavedData[info] = getRuleData(info)
            createPage(info)
        }
    }

    /**
     * Получить информацию о полях правила
     *
     * @param ruleName информация о правиле
     * @param fromDB получить правило из бд или из веб'а
     * @return информацию о полях правила
     */

    private fun getRuleData(ruleName: RuleInfo, fromDB: Boolean = false): RuleData {
        return if (fromDB) {
            ruleName.type.getDBData(ruleName)
        } else {
            ruleName.type.getMqttData(ruleName)
        }
    }

    /**
     * Сохраняет правила из веб'а
     *
     */
    override fun saveRules() {
        unsavedData.forEach {
            addRule(it.key, it.value)
        }
    }

    /**
     * Сохраняет правила в базу данных
     *
     */
    private fun saveDB() {
        unsavedData.forEach { (key, value) ->
            if (rule.contains(key) || slaveData.contains(key)) {
                value.save()
                if (value !is SlaveRuleData) {
                    ruleData[key] = value
                }
            }
        }
        unsavedData.clear()
    }

    /**
     * Получить название окно для настройки правила
     *
     * @param info информация о правиле
     */

    private fun getPageName(info: RuleInfo) = "${info}_rule"

    /**
     * Get page - получить окно виртуальное устройство
     *
     * @param pageName - информация о правиле
     */
    private fun getPage(pageName: RuleInfo) = VirtualDevice(
        getPageName(pageName),
        "правило $pageName",
        getJsFields(pageName).map { Cell(it.jsName, it.type) }.toMutableList(),
    )

    /**
     * Get rule info получить информацию о правиле которое хотим создать
     *
     * @return информацию о правиле
     */
    private fun getRuleInfo(): RuleInfo? {
        val type = RuleTypes.getRuleTypeByName(newRuleType) ?: return null
        return RuleInfo(newRuleName, type)
    }

    /**
     * Get js fields - получить поля правила
     *
     * @param pageName  информация о правиле
     * @return
     */
    private fun getJsFields(pageName: RuleInfo): List<FieldData> {
        return pageName.type.jsFields.get()
    }

    /**
     * Дополнительные правила которые можно использовать как dependency injection в данный момент нигде не используется
     *
     * @param info - информация о правиле
     * @return
     */

    fun getSlaveRuleData(info: RuleInfo): SlaveRuleData? {
        return slaveData[info]
    }

    /**
     * Add rule - добавить правило и вернуть информацию о создании
     *
     * @param info информация о правиле
     * @param data данные правила
     * @return
     */
    fun addRule(info: RuleInfo, data: RuleData): RuleCreateStatus {
        if (ruleNames.contains(info)) {
            return RuleCreateStatus.existError
        }
        if (!LaunchParams.CONFIGURATION_MODE) {
            if (data is SlaveRuleData) {
                if (data.complete()) {
                    ruleNames.add(info)
                    slaveData[info] = data
                } else {
                    return RuleCreateStatus.fieldsError
                }
            } else {
                val newRule = data.toRule()
                if (newRule != null) {
                    ruleNames.add(info)
                    ruleData[info] = data
                    rule[info] = newRule
                } else {
                    Logger.warn { "Rule $info not created" }
                    return RuleCreateStatus.fieldsError
                }
            }

            delete(getPageName(info))
        }
        data.save()
        return RuleCreateStatus.success
    }

    /**
     * Get rule data fields получить поля правила в формате map'ы
     *
     * @param info информация о правиле
     * @return
     */

    fun getRuleDataFields(info: RuleInfo): Map<String, String>? {
        val data = ruleData[info] ?: slaveData[info]
        return data?.fields?.map { it.key.toString() to it.value }?.toMap()
    }

    /**
     * Update rule обновить правило фактически удалить и добавить заново
     *
     * @param info информация о правиле
     * @param data поля правила
     * @return информацию об обновлении
     */
    fun updateRule(info: RuleInfo, data: RuleData): RuleCreateStatus {
        deleteRule(info)
        return addRule(info, data)
    }

    /**
     * Получить все существую правила из базы данных
     *
     */
    fun loadFromDB() {
        val existRules = RuleStorage.rules.getRules()
        existRules.forEach {
            val data = getRuleData(it, true)
            if (data is SlaveRuleData) {
                slaveData[it] = data
            } else {
                ruleData[it] = data
            }
            addRule(it, data)
        }
    }

    /**
     * Получить список существующих правил
     *
     * @return список правил
     */

    fun getRuleList(): List<RuleInfo> {
        return rule.keys.toList() + slaveData.keys.toList()
    }

    /**
     * Get room info - deprecated
     *
     * @param roomName
     * @return
     */
    fun getRoomInfo(roomName: String): Map<String, String> {
        return TODO()
    }

    /**
     * Update temp - deprecated
     *
     * @param roomName
     * @param temp
     * @return
     */
    fun updateTemp(roomName: String, temp: Double): Boolean {
        return true
        /*val room = rule[RuleInfo(roomName, RuleTypes.ROOM)] as? Room
        return if (room == null) {
            false
        } else {
            room.setPrefFromHttpTemp(temp)
            true
        }*/
    }

    class RuleCreateStatus private constructor(val status: String) {
        companion object {
            val success = RuleCreateStatus("success")
            val existError = RuleCreateStatus("this rule already exist")
            val fieldsError = RuleCreateStatus("invalid fields")
        }
    }

    companion object {
        private const val fileName = "/mnt/data/etc/wb-rules/RuleConfig.js"
        val ruleCore = RuleConfig(fileName)
        private const val MAIN_PAGE_TOPIC = "rule"
        private const val MAIN_PAGE_TITLE = "настройка правил"
        private const val ADD_BUTTON_NAME = "add rule"
        private const val SAVE_BUTTON_NAME = "save rule"
        private const val DELETE_BUTTON_NAME = "delete rule"
        private const val NEW_RULE_NAME = "name"
        private const val NEW_RULE_TYPE = "type"
    }
}
