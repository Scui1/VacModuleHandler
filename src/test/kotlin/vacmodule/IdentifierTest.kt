package vacmodule

import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class IdentifierTest {

    @Test
    fun identifierIgnoresTimestamp() {
        val uuid = UUID.randomUUID().toString()
        val identifier1 = VacModuleIdentifier(uuid, 0x2C00, 0x12345678,0x649217F0)
        val identifier2 = VacModuleIdentifier(uuid, 0x2C00, 0x12345678,0x649217F0)
        val identifier3 = VacModuleIdentifier(uuid, 0x2C00, 0x12345678,0x523245A9)

        assertEquals(identifier1, identifier2)
        assertEquals(identifier1, identifier3)
    }

    @Test
    fun formatsDateTimestampCorrectly() {
        val identifier = VacModuleIdentifier(UUID.randomUUID().toString(), 0x2C00, 0x12345678, 0x649217F0)

        assertEquals("20.06.2023 14:19:44", identifier.getFormattedTimestamp())
    }
}