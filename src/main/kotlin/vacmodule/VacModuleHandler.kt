package vacmodule

import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.request.*
import json.SubmitModuleRequest
import json.SubmitModuleResponse
import kotlinx.coroutines.runBlocking
import misc.FileToSend
import misc.sendEmbedWithFilesToDiscord

object VacModuleHandler {
    fun handleSubmit(request: SubmitModuleRequest, moduleBytes: ByteArray): SubmitModuleResponse {
        val vacModule = VacModule(moduleBytes, request.iceKeyAsString)
        if (!vacModule.canBeIdentified()) {
            return SubmitModuleResponse(false, "VAC module can't be identified.")
        }

        if (!vacModule.isKnownModule()) {
            sendModuleToDiscord(vacModule)
            return SubmitModuleResponse(true, "sent to discord")
        }


        return SubmitModuleResponse(true, "Something failed")
    }

    private fun sendModuleToDiscord(vacModule: VacModule) {
        val identifier = vacModule.identifier ?: return

        val message = """
            CodeView signature: `${identifier.signature}`
            Compiled at: ${identifier.formatBuildDate()}
            SizeOfCode: ${identifier.formatSizeOfCode()}
            RunFunc IceKey: `${vacModule.runfuncIceKey}`
        """
            .replace("\r\n", "\\n")
            .replace("\n", "\\n")

        runBlocking {
            sendEmbedWithFilesToDiscord("An unknown VAC module was streamed to a user.", message, 0xFFFF00, listOf(
                FileToSend(vacModule.moduleBytes, "${identifier.formatSizeOfCode()}_encrypted.dll"),
                FileToSend(vacModule.decrypt(), "${identifier.formatSizeOfCode()}_decrypted.dll")
            ))
        }
    }
}