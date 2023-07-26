package models

import kotlinx.coroutines.delay
import rules.Rule

class TaskManager {
    var id = 0L
    fun add(delay: Long, task: () -> Unit) {
        val curId = ++id
        Rule.addTask {
            delay(delay)
            if (curId == id) {
                task()
            }
        }
    }

    fun clear() {
        id++
    }
}
