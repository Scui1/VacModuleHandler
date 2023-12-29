import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import misc.discordWebhookUrl
import org.slf4j.LoggerFactory
import web.configureRouting

fun Application.main() {
    val logger = LoggerFactory.getLogger("Application")
    install(ContentNegotiation) {
        json()
    }
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            val exceptionMsg = cause.stackTraceToString()
            logger.error(exceptionMsg)
            call.respondText(text = exceptionMsg, status = HttpStatusCode.InternalServerError)
        }
    }
    configureRouting()
    discordWebhookUrl = environment.config.property("discordWebhookUrl").getString()
}