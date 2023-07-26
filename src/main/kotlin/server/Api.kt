package server

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import rules.RuleConfig
import rules.RuleInfo
import rules.RuleTypes
import rules.button.TapType

/**
 * Logic route основные ручки api
 *
 */
fun Routing.logicRoute() {
    val logic = RuleConfig.ruleCore
    /**
     * Derecated
     */
    route("room/") {
        get(
            "/control/{roomName}",
        ) {
            val roomName = call.parameters["roomName"] ?: return@get call.respondText(
                "bad parameter",
                status = HttpStatusCode.BadRequest,
            )
            val roomInfo = logic.getRoomInfo(roomName)
            call.respond(roomInfo)
        }
        put("/control/{roomName}") {
            val roomName = call.parameters["roomName"] ?: return@put call.respond(
                status = HttpStatusCode.BadRequest,
                mapOf("cause" to "bad parameter"),
            )
            val temp = call.receive<Map<String, String>>()["temp"]?.toDoubleOrNull()
            if (temp == null || !logic.updateTemp(roomName, temp)) {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    mapOf("cause" to "Room not found"),
                )
            } else {
                call.respond("OK")
            }
        }
    }
    route("rules") {
        /**
         * Получить список типов нажатий
         */
        get("/button/tapTypes") {
            call.respond(TapType.values().map { mapOf("label" to it.label, "value" to it.name) })
        }
        /**
         * Получить информацию о полях правил
         */
        get("/info") {
            call.respond(
                RuleTypes.values().associate {
                    it.name to it.jsFields.get()
                        .map { mapOf("name" to it.jsName, "type" to it.dataType.name, "label" to it.label) }
                },
            )
        }
        /**
         * Получить поля правила
         */

        get("/{type}/{name}/data") {
            val name = call.parameters["name"] ?: return@get call.respond(
                status = HttpStatusCode.BadRequest,
                mapOf("cause" to "bad parameter"),
            )
            val type = RuleTypes.getRuleTypeByName(call.parameters["type"] ?: "") ?: return@get call.respond(
                status = HttpStatusCode.BadRequest,
                mapOf("cause" to "bad parameter"),
            )
            val data = RuleConfig.ruleCore.getRuleDataFields(RuleInfo(name, type)) ?: return@get call.respond(
                status = HttpStatusCode.BadRequest,
                mapOf("cause" to "rule don't exist"),
            )
            call.respond(data)
        }
        /**
         * Обновить правило
         */

        put("/update") {
            val rule = call.receive<HttpRuleData>()
            var isCorrect = true
            try {
                val data = rule.type.getDataFromHttp(RuleInfo(rule.name, rule.type), rule.fields)
                val status: RuleConfig.RuleCreateStatus
                if (run {
                        status = logic.updateRule(RuleInfo(rule.name, rule.type), data)
                        status
                    } != RuleConfig.RuleCreateStatus.success
                ) {
                    return@put call.respond(
                        status = HttpStatusCode.BadRequest,
                        mapOf(
                            "cause" to status.status,
                        ),
                    )
                }
            } catch (ex: IllegalArgumentException) {
                isCorrect = false
            }
            if (!isCorrect) {
                return@put call.respond(status = HttpStatusCode.BadRequest, mapOf("cause " to " error rule info"))
            }
            call.respond(status = HttpStatusCode.Created, mapOf("status" to "OK"))
        }

        /**
         * Добавить новое правило
         */
        post("/add") {
            val newRule = call.receive<HttpRuleData>()
            var isCorrect = true
            try {
                val data = newRule.type.getDataFromHttp(RuleInfo(newRule.name, newRule.type), newRule.fields)
                val status: RuleConfig.RuleCreateStatus
                if (run {
                        status = logic.addRule(RuleInfo(newRule.name, newRule.type), data)
                        status
                    } != RuleConfig.RuleCreateStatus.success
                ) {
                    return@post call.respond(
                        status = HttpStatusCode.BadRequest,
                        mapOf(
                            "cause" to status.status,
                        ),
                    )
                }
            } catch (ex: IllegalArgumentException) {
                isCorrect = false
            }
            if (!isCorrect) {
                return@post call.respond(status = HttpStatusCode.BadRequest, mapOf("cause " to " error rule info"))
            }
            call.respond(status = HttpStatusCode.Created, mapOf("status" to "OK"))
        }

        /**
         * Удалить правило
         */

        delete("/{type}/{name}/delete") {
            val name = call.parameters["name"] ?: return@delete call.respond(
                status = HttpStatusCode.BadRequest,
                mapOf("cause" to "bad parameter"),
            )
            val type = RuleTypes.getRuleTypeByName(call.parameters["type"] ?: "") ?: return@delete call.respond(
                status = HttpStatusCode.BadRequest,
                mapOf("cause" to "bad parameter"),
            )
            val info = RuleInfo(name, type)
            if (!logic.getRuleList().contains(info)) {
                return@delete call.respondText("rule not found", status = HttpStatusCode.BadRequest)
            }
            logic.deleteRule(info)
            call.respond(mapOf("status" to "OK"))
        }
        get("/list") {
            call.respond(logic.getRuleList())
        }
    }
}
