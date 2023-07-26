import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import mu.KLogger
import mu.KotlinLogging
import server.logicRoute
import storage.DBRuleStorage
import java.lang.Thread.sleep

fun findArgs(args: Array<String>, name: String): Boolean {
    return args.contains(name)
}

fun main(args: Array<String>) {
    LaunchParams.LOCAL_LAUNCH = findArgs(args, "--local")
    LaunchParams.CONFIGURATION_MODE = findArgs(args, "--config")
    embeddedServer(Netty, port = 8080, module = Application::Config).start(true)
}

fun Application.mqtt() {
    val dbUrl =
        if (LaunchParams.LOCAL_LAUNCH) "jdbc:sqlite:identifier.sqlite" else "jdbc:sqlite:${LaunchParams.DATABASE_PATH}/identifier.sqlite"
    RuleStorage.rules = DBRuleStorage(dbUrl, "org.sqlite.JDBC").apply { init() }
    storage.TopicBase.connect()
    sleep(1000)
    if (!LaunchParams.CONFIGURATION_MODE) {
        rules.RuleConfig.ruleCore.loadFromDB()
    }
    routing {
        logicRoute()
    }
}

object Logger : KLogger by KotlinLogging.logger("")

object RuleStorage {
    lateinit var rules: DBRuleStorage
}

fun Application.Config() {
    install(ContentNegotiation) {
        json(

            Json {
                isLenient = true
                prettyPrint = true
            },
        )
    }
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Origin)
        allowHeader(HttpHeaders.Allow)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        allowHeader(HttpHeaders.AccessControlAllowHeaders)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        allowHeader(HttpHeaders.ContentType)
        allowHeader("*")
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }
    install(DefaultHeaders) {
        header("X-Engine", "Ktor") // will send this header with each response
    }
    mqtt()
}
