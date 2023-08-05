package decryption

class IceKeyRefactored(private val level: Int, private val key: ByteArray) {
    private val size = if (level < 1) 1 else level
    private val rounds = if (level < 1) 8 else level * 16
    private val keySchedule: Array<IntArray> = Array(rounds) { IntArray(3) }

    init {
        initKey(key)
    }

    // Encrypt a block of 8 bytes of data.
    fun encrypt(plaintext: ByteArray, ciphertext: ByteArray) {
        var i: Int
        var l = 0
        var r = 0
        i = 0
        while (i < 4) {
            l = l or (plaintext[i].toInt() and 0xff shl 24 - i * 8)
            r = r or (plaintext[i + 4].toInt() and 0xff shl 24 - i * 8)
            i++
        }
        i = 0
        while (i < rounds) {
            l = l xor roundFunc(r, keySchedule[i])
            r = r xor roundFunc(l, keySchedule[i + 1])
            i += 2
        }
        i = 0
        while (i < 4) {
            ciphertext[3 - i] = (r and 0xff).toByte()
            ciphertext[7 - i] = (l and 0xff).toByte()
            r = r ushr 8
            l = l ushr 8
            i++
        }
    }

    // Decrypt a block of 8 bytes of data.
    fun decrypt(ciphertext: ByteArray, plaintext: ByteArray) {
        var i: Int
        var l = 0
        var r = 0
        i = 0
        while (i < 4) {
            l = l or (ciphertext[i].toInt() and 0xff shl 24 - i * 8)
            r = r or (ciphertext[i + 4].toInt() and 0xff shl 24 - i * 8)
            i++
        }
        i = rounds - 1
        while (i > 0) {
            l = l xor roundFunc(r, keySchedule[i])
            r = r xor roundFunc(l, keySchedule[i - 1])
            i -= 2
        }
        i = 0
        while (i < 4) {
            plaintext[3 - i] = (r and 0xff).toByte()
            plaintext[7 - i] = (l and 0xff).toByte()
            r = r ushr 8
            l = l ushr 8
            i++
        }
    }

    // Set the key schedule of an ICE key.
    private fun initKey(key: ByteArray) {
        var i: Int
        val kb = IntArray(4)
        if (rounds == 8) {
            i = 0
            while (i < 4) {
                kb[3 - i] = (key[i * 2].toInt() and 0xff shl 8
                        or (key[i * 2 + 1].toInt() and 0xff))
                i++
            }
            scheduleBuild(kb, 0, 0)
            return
        }
        i = 0
        while (i < size) {
            var j: Int
            j = 0
            while (j < 4) {
                kb[3 - j] = (key[i * 8 + j * 2].toInt() and 0xff shl 8
                        or (key[i * 8 + j * 2 + 1].toInt() and 0xff))
                j++
            }
            scheduleBuild(kb, i * 8, 0)
            scheduleBuild(kb, rounds - 8 - i * 8, 8)
            i++
        }
    }

    // Set 8 rounds [n, n+7] of the key schedule of an ICE key.
    private fun scheduleBuild(kb: IntArray, n: Int, krot_idx: Int) {
        var i: Int
        i = 0
        while (i < 8) {
            var j: Int
            val kr = keyrot[krot_idx + i]
            val subkey = keySchedule[n + i]
            j = 0
            while (j < 3) {
                keySchedule[n + i][j] = 0
                j++
            }
            j = 0
            while (j < 15) {
                var k: Int
                val curr_sk = j % 3
                k = 0
                while (k < 4) {
                    val curr_kb = kb[kr + k and 3]
                    val bit = curr_kb and 1
                    subkey[curr_sk] = subkey[curr_sk] shl 1 or bit
                    kb[kr + k and 3] = curr_kb ushr 1 or (bit xor 1 shl 15)
                    k++
                }
                j++
            }
            i++
        }
    }

    // The single round ICE f function.
    private fun roundFunc(p: Int, subkey: IntArray): Int {
        val tl: Int
        val tr: Int
        var al: Int
        var ar: Int
        tl = p ushr 16 and 0x3ff or (p ushr 14 or (p shl 18) and 0xffc00)
        tr = p and 0x3ff or (p shl 2 and 0xffc00)

        // al = (tr & subkey[2]) | (tl & ~subkey[2]);
        // ar = (tl & subkey[2]) | (tr & ~subkey[2]);
        al = subkey[2] and (tl xor tr)
        ar = al xor tr
        al = al xor tl
        al = al xor subkey[0]
        ar = ar xor subkey[1]
        return (spBox[0][al ushr 10] or spBox[1][al and 0x3ff]
                or spBox[2][ar ushr 10] or spBox[3][ar and 0x3ff])
    }

    companion object {
        private val sMod = arrayOf(
            intArrayOf(333, 313, 505, 369),
            intArrayOf(379, 375, 319, 391),
            intArrayOf(361, 445, 451, 397),
            intArrayOf(397, 425, 395, 505)
        )
        private val sXor = arrayOf(
            intArrayOf(0x83, 0x85, 0x9b, 0xcd),
            intArrayOf(0xcc, 0xa7, 0xad, 0x41),
            intArrayOf(0x4b, 0x2e, 0xd4, 0x33),
            intArrayOf(0xea, 0xcb, 0x2e, 0x04)
        )
        private val pBox = intArrayOf(
            0x00000001, 0x00000080, 0x00000400, 0x00002000,
            0x00080000, 0x00200000, 0x01000000, 0x40000000,
            0x00000008, 0x00000020, 0x00000100, 0x00004000,
            0x00010000, 0x00800000, 0x04000000, 0x20000000,
            0x00000004, 0x00000010, 0x00000200, 0x00008000,
            0x00020000, 0x00400000, 0x08000000, 0x10000000,
            0x00000002, 0x00000040, 0x00000800, 0x00001000,
            0x00040000, 0x00100000, 0x02000000, -0x80000000
        )
        private val keyrot = intArrayOf(
            0, 1, 2, 3, 2, 1, 3, 0,
            1, 3, 2, 0, 3, 1, 0, 2
        )
        private val spBox = spBoxInit()

        private fun gf_mult(a: Int, b: Int, m: Int): Int {
            var a = a
            var b = b
            var res = 0
            while (b != 0) {
                if (b and 1 != 0) res = res xor a
                a = a shl 1
                b = b ushr 1
                if (a >= 256) a = a xor m
            }
            return res
        }

        // 8-bit Galois Field exponentiation.
        // Raise the base to the power of 7, modulo m.
        private fun gf_exp7(b: Int, m: Int): Int {
            var x: Int
            if (b == 0) return 0
            x = gf_mult(b, b, m)
            x = gf_mult(b, x, m)
            x = gf_mult(x, x, m)
            return gf_mult(b, x, m)
        }

        // Carry out the ICE 32-bit permutation.
        private fun perm32(x: Int): Int {
            var x = x
            var res = 0
            var i = 0
            while (x != 0) {
                if (x and 1 != 0) res = res or pBox[i]
                i++
                x = x ushr 1
            }
            return res
        }

        // Initialise the substitution/permutation boxes.
        private fun spBoxInit(): Array<IntArray> {
            var i: Int
            var newSpBox= Array(4) { IntArray(1024) }
            i = 0
            while (i < 1024) {
                val col = i ushr 1 and 0xff
                val row = i and 0x1 or (i and 0x200 ushr 8)
                var x: Int
                x = gf_exp7(col xor sXor[0][row], sMod[0][row]) shl 24
                newSpBox[0][i] = perm32(x)
                x = gf_exp7(col xor sXor[1][row], sMod[1][row]) shl 16
                newSpBox[1][i] = perm32(x)
                x = gf_exp7(col xor sXor[2][row], sMod[2][row]) shl 8
                newSpBox[2][i] = perm32(x)
                x = gf_exp7(col xor sXor[3][row], sMod[3][row])
                newSpBox[3][i] = perm32(x)
                i++
            }
            return newSpBox
        }
    }
}