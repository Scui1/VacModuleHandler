package vacmodule

import decryption.decryptVacModule
import org.slf4j.LoggerFactory
import pefile.PEFile

private val logger = LoggerFactory.getLogger("VacModule")

class VacModule(val moduleBytes: ByteArray, val runfuncIceKey: String = "") {
    private val peFile = PEFile(moduleBytes)
    val identifier = VacModuleIdentifier.fromModule(peFile)

    fun canBeIdentified(): Boolean {
        return identifier != null;
    }

    fun isKnownModule(): Boolean {
        return identifier?.let { KnownModulesHolder.isKnownModule(it) } ?: false
    }

    fun decrypt(): ByteArray? {
        return decryptVacModule(peFile, runfuncIceKey).getOrElse { exception ->
            logger.error("Decryption of module with sizeOfCode ${identifier?.getFormattedSizeOfCode()} failed: ${exception.message}")
            return null
        }
    }

}