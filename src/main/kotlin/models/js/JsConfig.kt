package models.js

import rules.RuleInfo

abstract class JsConfig(val mainPage: VirtualDevice, path: String) {
    private val builder = JsBuilder(path)

    init {
        builder.add(mainPage)
    }

    fun add(page: VirtualDevice) {
        builder.add(page)
    }

    fun delete(name: String) {
        builder.delete(name)
    }

    fun set(pages: List<VirtualDevice>) {
        builder.set(pages + mainPage)
    }

    abstract fun saveRules()
    abstract fun checkButton()
    abstract fun addPage()
    abstract fun createPage(pageName: RuleInfo)
}
