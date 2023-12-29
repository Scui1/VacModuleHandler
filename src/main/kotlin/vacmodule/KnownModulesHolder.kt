package vacmodule

object KnownModulesHolder {
    private val knownModules = listOf(
        VacModuleIdentifier("4D49776C-CEBF-4958-9337-F050763BA123", 0x2C00, 0x649217F0),
        VacModuleIdentifier("8BB677F5-F76E-497F-AEC4-0DF30A654B1A", 0xA000, 0x6408E03E),
        VacModuleIdentifier("DFFA4892-5D12-4097-885A-85CDFC71C9F0", 0xC000, 0x633BB056),
        VacModuleIdentifier("07B71E6D-4D55-4DB6-B3EC-4D05512F710A", 0x6400, 0x633BA1F0),
        VacModuleIdentifier("C34792EF-BAEC-443C-9B53-6C06001E9EC0", 0x9200, 0x633BB1E6),
        VacModuleIdentifier("B0B74BCE-5F6C-4DDF-B670-5EA0907FC82D", 0x4A00, 0x633BA16E),
        VacModuleIdentifier("F582C191-D083-4BB5-924D-C4D53BC6D52A", 0x5800, 0x633BA0F6),
    )

    fun isKnownModule(identifier: VacModuleIdentifier): Boolean {
        return knownModules.find { it == identifier } != null
    }
}