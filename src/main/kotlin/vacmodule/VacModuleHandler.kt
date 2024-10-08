package vacmodule

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
            KnownModulesHolder.declareModuleAsKnown(vacModule)
            return SubmitModuleResponse(true, "sent to discord")
        }

        return SubmitModuleResponse(true, "Module already known.")
    }

    private fun sendModuleToDiscord(vacModule: VacModule) {
        val identifier = vacModule.identifier ?: return

        val message = """
            CodeView signature: `${identifier.signature}`
            SizeOfCode: ${identifier.getFormattedSizeOfCode()}
            CRC32 of .text: ${identifier.getFormattedCodeHash()}
            Compiled at: ${identifier.getFormattedTimestamp()}
            RunFunc IceKey: `${vacModule.runfuncIceKey}`
        """
            .replace("\r\n", "\\n")
            .replace("\n", "\\n")

        runBlocking {
            sendEmbedWithFilesToDiscord("An unknown VAC module was streamed to a user.", message, 0xFFFF00,
                listOfNotNull(
                    FileToSend(vacModule.moduleBytes, "${identifier.getFormattedSizeOfCode()}_encrypted.dll"),
                    vacModule.decrypt()?.let { FileToSend(it, "${identifier.getFormattedSizeOfCode()}_decrypted.dll") }
                ))
        }
    }
}