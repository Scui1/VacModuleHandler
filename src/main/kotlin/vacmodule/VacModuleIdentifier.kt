package vacmodule

import kotlinx.serialization.Serializable
import pefile.PEFile
import pefile.datadirectory.DataDirectoryType.DEBUG_DIRECTORY
import pefile.datadirectory.directories.DebugDirectory
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Serializable
class VacModuleIdentifier(val signature: String, val sizeOfCode: Int, val timeDateStamp: Int) {

    fun formatBuildDate(): String {
        return DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
            .withZone(ZoneId.of("US/Pacific"))
            .format(Instant.ofEpochSecond(timeDateStamp.toLong()))
    }

    fun formatSizeOfCode(): String {
        return sizeOfCode.toString(16).uppercase()
    }

    override fun equals(other: Any?): Boolean {
        return other is VacModuleIdentifier && other.signature == signature && other.sizeOfCode == sizeOfCode && other.timeDateStamp == timeDateStamp
    }

    companion object {
        fun fromModule(peFile: PEFile): VacModuleIdentifier? {
            val debugDirectory = peFile.getDataDirectoryByType(DEBUG_DIRECTORY)
            if (debugDirectory !is DebugDirectory) {
                return null
            }

            return VacModuleIdentifier(debugDirectory.formattedSignature(), peFile.getSizeOfCode(), debugDirectory.timeDateStamp)
        }
    }
}