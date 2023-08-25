import decryption.VacModule
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import json.DecryptRequest
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("Application")

fun Application.main() {
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
    routing {

        get("/alive") {
            call.respond(HttpStatusCode.OK, true)
        }
        post("/decryptModule") {
            val multipart = call.receiveMultipart().readAllParts()
            val payloadJsonParam = multipart.filterIsInstance<PartData.FormItem>().find { partData -> "payloadJson" == partData.name }
            val moduleBytesParam = multipart.filterIsInstance<PartData.FileItem>().find { partData -> "moduleBytes" == partData.name }

            if (payloadJsonParam == null) {
                call.respondText("Missing \"payloadJson\" parameter.", ContentType.Text.Plain, HttpStatusCode.BadRequest)
                return@post
            } else if (moduleBytesParam == null) {
                call.respondText("Missing \"moduleBytes\" parameter.", ContentType.Text.Plain, HttpStatusCode.BadRequest)
                return@post
            }

            val moduleBytes = moduleBytesParam.streamProvider().readBytes()
            val decryptRequest = Json.decodeFromString<DecryptRequest>(payloadJsonParam.value)

            val vacModule = VacModule(moduleBytes, decryptRequest.iceKeyAsString)
            call.respond(HttpStatusCode.OK, vacModule.decrypt())
        }
    }
}