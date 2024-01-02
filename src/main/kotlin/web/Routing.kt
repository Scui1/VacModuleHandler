package web

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import json.SubmitModuleRequest
import json.SubmitModuleResponse
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import vacmodule.VacModule
import vacmodule.VacModuleHandler

fun Application.configureRouting() {
    routing {

        get("/alive") {
            call.respond(HttpStatusCode.OK, true)
        }

        get("/getModuleIdentifier") {
            val multipart = call.receiveMultipart().readAllParts()
            val moduleBytesParam =
                multipart.filterIsInstance<PartData.FileItem>().find { partData -> "moduleBytes" == partData.name }
            if (moduleBytesParam == null) {
                call.respondText(
                    "Missing \"moduleBytes\" parameter.",
                    ContentType.Text.Plain,
                    HttpStatusCode.BadRequest
                )
                return@get
            }

            val moduleBytes = moduleBytesParam.streamProvider().readBytes()
            val identifier = VacModule(moduleBytes).identifier
            if (identifier != null) {
                call.respond(HttpStatusCode.OK, identifier)
            } else {
                call.respondText(status = HttpStatusCode.InternalServerError, text = "Couldn't generate identifier.")
            }

        }

        post("/submitModule") {
            val multipart = call.receiveMultipart().readAllParts()
            val payloadJsonParam =
                multipart.filterIsInstance<PartData.FormItem>().find { partData -> "payloadJson" == partData.name }
            val moduleBytesParam =
                multipart.filterIsInstance<PartData.FileItem>().find { partData -> "moduleBytes" == partData.name }

            if (payloadJsonParam == null) {
                call.respondText(
                    "Missing \"payloadJson\" parameter.",
                    ContentType.Text.Plain,
                    HttpStatusCode.BadRequest
                )
                return@post
            } else if (moduleBytesParam == null) {
                call.respondText(
                    "Missing \"moduleBytes\" parameter.",
                    ContentType.Text.Plain,
                    HttpStatusCode.BadRequest
                )
                return@post
            }
            val moduleBytes = moduleBytesParam.streamProvider().readBytes()
            val submitModuleRequest = Json.decodeFromString<SubmitModuleRequest>(payloadJsonParam.value)

            val response = VacModuleHandler.handleSubmit(submitModuleRequest, moduleBytes)
            call.respondText(response.message, ContentType.Text.Plain, if (response.success) HttpStatusCode.OK else HttpStatusCode.InternalServerError)

        }
    }
}