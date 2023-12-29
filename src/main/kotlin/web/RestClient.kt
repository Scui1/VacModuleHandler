package web

import io.ktor.client.statement.*
import io.ktor.http.*

data class RestResponse(val httpResponse: HttpResponse?, val isSuccess: Boolean, val errorMessage: String) {}

suspend fun executeHttpRequest(block: suspend () -> HttpResponse): RestResponse {
    var exceptionMessage = ""
    val httpResponse = try {
        block()
    } catch (exception: Throwable) {
        exceptionMessage = exception.stackTraceToString()
        null
    }

    val responseSuccess = httpResponse?.status?.isSuccess().let { it == true }
    if (exceptionMessage.isEmpty()) {
        exceptionMessage = httpResponse?.bodyAsText() ?: httpResponse?.toString() ?: exceptionMessage
    }

    return RestResponse(httpResponse, responseSuccess, exceptionMessage)
}
