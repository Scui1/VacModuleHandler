package decryption

import org.slf4j.LoggerFactory
import patternsearching.patternBytesFromString
import patternsearching.searchPattern
import pefile.PEFile

class VacModule(val moduleBytes: ByteArray, val runfuncIceKey: String) {
    private val logger = LoggerFactory.getLogger("VacModule")

    fun decrypt(): ByteArray {

        val peFile = PEFile(moduleBytes)
        val textSection = peFile.getSectionByName(".text") ?: return byteArrayOf()
        val dataSection = peFile.getSectionByName(".data") ?: return byteArrayOf()

        val encryptedImportsSizeAddress = searchPattern(peFile, textSection, patternBytesFromString("68 ?? ?? ?? ?? 8B D1"), 1)
        if (encryptedImportsSizeAddress == 0) {
            logger.error("Couldn't find pattern for encrypted imports size.")
            return byteArrayOf()
        }
        val encryptedImportsSize = peFile.readInt(encryptedImportsSizeAddress + 1)

        val encryptedImportsStartAddress = searchPattern(peFile, textSection, patternBytesFromString("51 B9 ?? ?? ?? ?? 68"), 1) // pattern can differ sometimes
        if (encryptedImportsStartAddress == 0) {
            logger.error("Couldn't find pattern for encrypted imports.")
            return byteArrayOf()
        }
        val encryptedImportsStartVirtual = peFile.readInt(encryptedImportsStartAddress + 2)
        val encryptedImportsStart = peFile.convertVirtualOffsetToRawOffset(encryptedImportsStartVirtual - peFile.getImageBase())

        val primaryIceKeyBase =  searchPattern(peFile, dataSection, patternBytesFromString("00 00 58 05 00 00 00 00 00 00 00 00 00 00"), 1)
        if (primaryIceKeyBase == 0) {
            logger.error("Couldn't find pattern for primary ice key.")
            return byteArrayOf()
        }

        val primaryIceKeyPattern = searchPattern(peFile, dataSection, patternBytesFromString("01 00 00 00"), 1, primaryIceKeyBase, 100)
        if (primaryIceKeyPattern == 0) {
            logger.error("Couldn't find pattern for primary ice key.")
            return byteArrayOf()
        }
        val primaryIceKey = peFile.read(primaryIceKeyPattern + 8, 8)

        val iceKey = IceKey(1, primaryIceKey)

        // decrypt ice key we received in runfunc with key we received in vac module
        val runfuncIceKeyBytes = runfuncIceKey.split(" ").map { it.toUByte(16).toByte() }.toByteArray()
        val secondaryIceKey = iceKey.decrypt(runfuncIceKeyBytes)

        iceKey.setKey(secondaryIceKey)

        for (encryptedAddress in encryptedImportsStart..< encryptedImportsStart + encryptedImportsSize step 8) {
            val encryptedBytes = peFile.read(encryptedAddress, 8)
            val decryptedBytes = iceKey.decrypt(encryptedBytes)
            peFile.write(encryptedAddress, decryptedBytes)
        }

        return peFile.bytes
    }

}