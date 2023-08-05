package decryption

import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class IceKeyTest {
    companion object {
        private val KEY_BYTES = byteArrayOf(0xA6.toByte(), 0x23, 0x91.toByte(), 0x03, 0x7C, 0x75, 0x58, 0x7A)
        private val DECRYPTED = byteArrayOf(0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8)
        private val ENCRYPTED = byteArrayOf(0x6b, 0xf7.toByte(), 0x14, 0xfc.toByte(), 0x59, 0x06, 0x47, 0xff.toByte())
    }

    private val iceKey = IceKey(1, KEY_BYTES)

    @Test
    fun testDecrypt() {
        val decryptedBytes = iceKey.decrypt(ENCRYPTED)

        assertContentEquals(DECRYPTED, decryptedBytes)
    }

    @Test
    fun testEncrypt() {
        val encryptedBytes = iceKey.encrypt(DECRYPTED)

        assertContentEquals(ENCRYPTED, encryptedBytes)
    }
}