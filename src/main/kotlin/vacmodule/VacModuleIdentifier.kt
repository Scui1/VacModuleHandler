package vacmodule

import kotlinx.serialization.Serializable
import pefile.PEFile
import pefile.datadirectory.DataDirectoryType.DEBUG_DIRECTORY
import pefile.datadirectory.DataDirectoryType.EXPORT_DIRECTORY
import pefile.datadirectory.directories.DebugDirectory
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.zip.CRC32

@Serializable
class VacModuleIdentifier(val signature: String, val sizeOfCode: Int, val codeHash: Long, private val timeDateStamp: Int) {

    fun getFormattedTimestamp(): String {
        return DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
            .withZone(ZoneId.of("US/Pacific"))
            .format(Instant.ofEpochSecond(timeDateStamp.toLong()))
    }

    fun getFormattedSizeOfCode(): String {
        return sizeOfCode.toString(16).uppercase()
    }

    fun getFormattedCodeHash(): String {
        return codeHash.toString(16).uppercase()
    }

    override fun equals(other: Any?): Boolean {
        return other is VacModuleIdentifier && other.signature == signature && other.sizeOfCode == sizeOfCode && other.codeHash == codeHash
    }

    override fun hashCode(): Int {
        return 31 * signature.hashCode() + 31 * sizeOfCode.hashCode() + 31 * codeHash.hashCode()
    }

    companion object {
        private fun PEFile.calculateCodeHash(): Long {
            if (this.getSectionByName(".text") == null) {
                return 0
            }

            // At the start of .text there is not actual code, but all kinds of directoryEntries.
            // DebugDirectory is the last part before actual code starts
            val debugDirectory = this.getDataDirectoryByType(DEBUG_DIRECTORY) as? DebugDirectory ?: return 0
            val lastDebugEntry = debugDirectory.entries.maxByOrNull { it.pointerToRawData } ?: return 0
            val startOfActualCode = lastDebugEntry.pointerToRawData + lastDebugEntry.size

            // Actual code stops before end of .text. After actual code there is the ExportDirectory, so we use this as border
            val exportDirectoryDescription = this.getDataDirectoryDescriptions().find { it.type == EXPORT_DIRECTORY } ?: return 0
            val endOfActualCode = exportDirectoryDescription.rawAddress - 1

            val codeBytes = this.read(startOfActualCode, endOfActualCode - startOfActualCode)

            val crcCalculator = CRC32()
            crcCalculator.update(codeBytes)
            return crcCalculator.value
        }

        fun fromModule(peFile: PEFile): VacModuleIdentifier? {
            val debugDirectory = peFile.getDataDirectoryByType(DEBUG_DIRECTORY)
            if (debugDirectory !is DebugDirectory) {
                return null
            }

            return VacModuleIdentifier(
                debugDirectory.getCodeViewSignature(),
                peFile.getSizeOfCode(),
                peFile.calculateCodeHash(),
                debugDirectory.getCodeViewDateTimestamp()
            )
        }
    }
}