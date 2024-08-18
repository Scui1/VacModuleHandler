package decryption

import patternsearching.patternBytesFromString
import patternsearching.searchPattern
import pefile.PEFile

private class DecryptionFailureException(message: String): Exception(message)

fun decryptVacModule(peFile: PEFile, runfuncIceKey: String): Result<ByteArray> {
    val textSection =
        peFile.getSectionByName(".text") ?: return failedDecryption("Module has no .text section")
    val dataSection =
        peFile.getSectionByName(".data") ?: return failedDecryption("Module has no .data section")

    val encryptedImportsSizeAddress =
        searchPattern(peFile, textSection, patternBytesFromString("68 ?? ?? ?? ?? 8B D1"), 1)
    if (encryptedImportsSizeAddress == 0) {
        return failedDecryption("Couldn't find pattern for encrypted imports size.")
    }
    val encryptedImportsSize = peFile.readInt(encryptedImportsSizeAddress + 1)

    val encryptedImportsStartAddress = searchPattern(
        peFile,
        textSection,
        patternBytesFromString("51 B9 ?? ?? ?? ?? 68"),
        1
    ) // pattern can differ sometimes
    if (encryptedImportsStartAddress == 0) {
        return failedDecryption("Couldn't find pattern for encrypted imports.")
    }
    val encryptedImportsStartVirtual = peFile.readInt(encryptedImportsStartAddress + 2)
    val encryptedImportsStart =
        peFile.convertVirtualOffsetToRawOffset(encryptedImportsStartVirtual - peFile.getImageBase())

    val primaryIceKeyBase =
        searchPattern(peFile, dataSection, patternBytesFromString("00 00 58 05 00 00 00 00 00 00 00 00 00 00"), 1)
    if (primaryIceKeyBase == 0) {
        return failedDecryption("Couldn't find pattern for primary ice key base.")
    }

    val primaryIceKeyPattern =
        searchPattern(peFile, dataSection, patternBytesFromString("01 00 00 00"), 1, primaryIceKeyBase, 100)
    if (primaryIceKeyPattern == 0) {
        return failedDecryption("Couldn't find pattern for primary ice key.")
    }
    val primaryIceKey = peFile.read(primaryIceKeyPattern + 8, 8)

    val iceKey = IceKey(1, primaryIceKey)

    // decrypt ice key we received in runfunc with key we received in vac module
    val runfuncIceKeyBytes = runfuncIceKey.split(" ").map { it.toUByte(16).toByte() }.toByteArray()
    val secondaryIceKeyBytes = iceKey.decrypt(runfuncIceKeyBytes)

    iceKey.setKey(secondaryIceKeyBytes)

    val newPeFile = PEFile.copyOf(peFile)
    for (encryptedAddress in encryptedImportsStart..<encryptedImportsStart + encryptedImportsSize step 8) {
        val encryptedBytes = peFile.read(encryptedAddress, 8)
        val decryptedBytes = iceKey.decrypt(encryptedBytes)
        newPeFile.write(encryptedAddress, decryptedBytes)
    }

    return Result.success(newPeFile.bytes)
}

private fun failedDecryption(reason: String): Result<ByteArray> = Result.failure(DecryptionFailureException(reason))