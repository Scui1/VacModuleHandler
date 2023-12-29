package vacmodule

import decryption.decryptVacModule
import pefile.PEFile

class VacModule(val moduleBytes: ByteArray, val runfuncIceKey: String = "") {
    private val peFile = PEFile(moduleBytes)
    val identifier = VacModuleIdentifier.fromModule(peFile)

    fun canBeIdentified(): Boolean {
        return identifier != null;
    }

    fun isKnownModule(): Boolean {
        return identifier?.let { KnownModulesHolder.isKnownModule(it) } ?: false
    }

    fun decrypt(): ByteArray {
        return decryptVacModule(peFile, runfuncIceKey)
    }

}