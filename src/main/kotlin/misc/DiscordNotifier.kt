package misc

import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.slf4j.LoggerFactory
import web.executeHttpRequest

var discordWebhookUrl = ""
private val logger = LoggerFactory.getLogger("DiscordNotifier")

data class FileToSend(val bytes: ByteArray, val name: String)

suspend fun sendEmbedWithFilesToDiscord(title: String, message: String, color: Int, files: List<FileToSend>) {
    val jsonString = """
    {
        "embeds": [{
            "title": "$title",
            "description": "$message",
            "color": $color        
        }]
    }
    """

    val response = executeHttpRequest {
        sendMessageWithBinaryData(jsonString, files)
    }

    if (!response.isSuccess)
        logger.error("Sending to discord failed: ${response.errorMessage}")
}

private suspend fun sendMessageWithBinaryData(jsonString: String, files: List<FileToSend>): HttpResponse {
    return HttpClient(Java) {}.use { client ->
        client.submitFormWithBinaryData(discordWebhookUrl,
            formData {
                append("payload_json", jsonString)
                files.forEach {
                    append(it.name, it.bytes, Headers.build {
                        append(HttpHeaders.ContentType, "application/octet-stream")
                        append(HttpHeaders.ContentDisposition, "filename=${it.name}")
                    })
                }

            })

    }
}