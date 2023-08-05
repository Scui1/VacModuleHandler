import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HexFormat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestOriginalIceKey {

    private static final byte[] KEY_BYTES = new byte[]{(byte) 0xA6, 0x23, (byte) 0x91, 0x03, 0x7C, 0x75, 0x58, 0x7A};
    private static final byte[] DECRYPTED = new byte[]{ 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8};
    private static final byte[] ENCRYPTED = new byte[]{ 0x6b, (byte) 0xf7, 0x14, (byte) 0xfc, 0x59, 0x06, 0x47,
            (byte) 0xff};
    private final OriginalIceKey originalIceKey = new OriginalIceKey(1);

    @BeforeEach
    void setup() {
        originalIceKey.set(KEY_BYTES);
    }

    @Test
    void testEncrypt() {
        var bytesEncrypted = new byte[DECRYPTED.length];

        originalIceKey.encrypt(DECRYPTED, bytesEncrypted);

        assertArrayEquals(ENCRYPTED, bytesEncrypted);
    }

    @Test
    void testDecrypt() {
        var bytesDecrypted = new byte[ENCRYPTED.length];

        originalIceKey.decrypt(ENCRYPTED, bytesDecrypted);

        assertArrayEquals(DECRYPTED, bytesDecrypted);
    }
}
