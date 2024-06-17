package ivorius.psychedelicraft.world.gen.loot;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import ivorius.psychedelicraft.Psychedelicraft;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.util.Identifier;

public interface PSLootTableEntryType {
    /*LootPoolEntryType LOOT_TABLE_EXTENSION = register("loot_table_extension", LootExtensionEntry.CODEC);

    private static LootPoolEntryType register(String id, MapCodec<? extends LootPoolEntry> codec) {
        return Registry.register(Registries.LOOT_POOL_ENTRY_TYPE, Psychedelicraft.id(id), new LootPoolEntryType(codec));
    }*/

    static void bootstrap() {
        Map<Identifier, Identifier> extentionTableIds = new HashMap<>();
        LootTableEvents.MODIFY.register((key, supplier, source) -> {
            Identifier id = key.getValue();

            final boolean isVillagerChest = id.getPath().contains("village");
            if ((isVillagerChest || Psychedelicraft.getConfig().balancing.worldGeneration.villageChests())
            || (!isVillagerChest || Psychedelicraft.getConfig().balancing.worldGeneration.dungeonChests())) {
                if ("psychedelicraftmc".equalsIgnoreCase(id.getPath())) {
                    extentionTableIds.put(Identifier.ofVanilla(id.getPath()), id);
                }
                //supplier.pool(LootPool.builder()
                  //      .with(LootExtensionEntry.builder(RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of("psychedelicraftmc", id.getPath())))));
            }
        });
        LootTableEvents.ALL_LOADED.register((resourceManager, registry) -> {
            extentionTableIds.forEach((base, extra) -> {
                registry.getOrEmpty(base).ifPresent(table -> {
                    registry.getOrEmpty(extra).ifPresent(extraTable -> {
                        table.pools = Stream.concat(table.pools.stream(), extraTable.pools.stream()).toList();
                    });
                });
            });
            extentionTableIds.clear();
        });
    }
}
