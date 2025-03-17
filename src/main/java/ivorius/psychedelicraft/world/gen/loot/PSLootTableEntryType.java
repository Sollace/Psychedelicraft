package ivorius.psychedelicraft.world.gen.loot;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import ivorius.psychedelicraft.Psychedelicraft;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.util.Identifier;

public interface PSLootTableEntryType {
    static void bootstrap() {
        Map<Identifier, Identifier> extentionTableIds = new HashMap<>();
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            Identifier id = key.getValue();

            final boolean isVillagerChest = id.getPath().contains("village");
            if ((isVillagerChest || Psychedelicraft.getConfig().worldGeneration.get().villageChests())
            || (!isVillagerChest || Psychedelicraft.getConfig().worldGeneration.get().dungeonChests())) {
                if (Psychedelicraft.VANILLA_EXTENSIONS_NAMESPACE.equalsIgnoreCase(id.getNamespace())) {
                    extentionTableIds.put(Identifier.ofVanilla(id.getPath()), id);
                }
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
