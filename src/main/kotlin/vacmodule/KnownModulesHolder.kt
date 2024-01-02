package vacmodule

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object KnownModulesHolder {
    private val json = Json { this.prettyPrint = true }
    private var knownModules = mutableListOf<VacModuleIdentifier>()
    private var filePath: String = ""

    fun isKnownModule(identifier: VacModuleIdentifier): Boolean {
        return knownModules.find { it == identifier } != null
    }

    fun declareModuleAsKnown(module: VacModule) {
        knownModules.add(module.identifier!!)
        writeKnownModulesToFile()
    }

    fun readKnownModulesFromFile(filePath: String) {
        this.filePath = filePath
        val fileContent = File(filePath).readText()
        knownModules = Json.decodeFromString<MutableList<VacModuleIdentifier>>(fileContent)
    }

    private fun writeKnownModulesToFile() {
        val newFileContent = json.encodeToString(knownModules)
        File(filePath).writeText(newFileContent)
    }
}