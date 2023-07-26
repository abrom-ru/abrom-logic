package rules

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import models.js.JsBuilder

/**
 * Интерфейс который должны реализовывать все правила
 *
 * @constructor Create empty Rule
 */
interface Rule {

    /**
     * Delete rule from database, delete callbacks and pages from ui
     *
     */
    fun deleteRule()

    companion object {
        private const val path = "/mnt/data/etc/wb-rules/rules_devices.js"
        val builder = JsBuilder(path)
        fun addTask(task: Task): Job {
            return addTask { task.execute() }
        }

        fun addTask(task: suspend () -> Unit): Job {
            return GlobalScope.launch { task.invoke() }
        }
    }
}

fun interface Task {
    suspend fun execute()
}
