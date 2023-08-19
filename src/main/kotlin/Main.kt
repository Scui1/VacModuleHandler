import decryption.IceKey
import org.slf4j.LoggerFactory
import patternsearching.patternBytesFromString
import patternsearching.searchPattern
import pefile.PEFile
import pefile.fromFile
import pefile.writeToFile

private val logger = LoggerFactory.getLogger("main")

fun main(args: Array<String>) {
    val fileName = args[0]
    val iceKeyString = args[1]

    val peFile = PEFile.fromFile(fileName) ?: return
    val textSection = peFile.getSectionByName(".text") ?: return
    val dataSection = peFile.getSectionByName(".data") ?: return

    val encryptedImportsSizeAddress = searchPattern(peFile, textSection, patternBytesFromString("68 ?? ?? ?? ?? 8B D1"), 1)
    if (encryptedImportsSizeAddress == 0) {
        logger.error("Couldn't find pattern for encrypted imports size.")
        return
    }
    val encryptedImportsSize = peFile.readInt(encryptedImportsSizeAddress + 1)

    val encryptedImportsStartAddress = searchPattern(peFile, textSection, patternBytesFromString("51 B9 ?? ?? ?? ?? 68"), 1) // pattern can differ sometimes
    if (encryptedImportsStartAddress == 0) {
        logger.error("Couldn't find pattern for encrypted imports.")
        return
    }
    val encryptedImportsStartVirtual = peFile.readInt(encryptedImportsStartAddress + 2)
    val encryptedImportsStart = peFile.convertVirtualOffsetToRawOffset(encryptedImportsStartVirtual - peFile.getImageBase())

    val primaryIceKeyPattern = searchPattern(peFile, dataSection, patternBytesFromString("00 00 00 00 00 00 00 00 00 00 00 00 00 00 ?? ?? ?? ?? 01 00 00 00"), 1)
    if (primaryIceKeyPattern == 0) {
        logger.error("Couldn't find pattern for primary ice key.")
        return
    }
    val primaryIceKey = peFile.read(primaryIceKeyPattern + 0x1A, 8)

    val iceKey = IceKey(1, primaryIceKey)

    // decrypt ice key we received in runfunc with key we received in vac module
    val runfuncIceKeyBytes = iceKeyString.split(" ").map { it.toUByte(16).toByte() }.toByteArray()
    val secondaryIceKey = iceKey.decrypt(runfuncIceKeyBytes)

    iceKey.setKey(secondaryIceKey)

    for (encryptedAddress in encryptedImportsStart..< encryptedImportsStart + encryptedImportsSize step 8) {
        val encryptedBytes = peFile.read(encryptedAddress, 8)
        val decryptedBytes = iceKey.decrypt(encryptedBytes)
        peFile.write(encryptedAddress, decryptedBytes)
    }

    peFile.writeToFile("B:\\Downloads\\vacmodule_decrypted.dll");
}