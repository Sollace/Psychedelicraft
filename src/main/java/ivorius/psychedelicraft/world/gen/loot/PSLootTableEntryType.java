package ivorius.psychedelicraft.world.gen.loot;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import ivorius.psychedelicraft.Psychedelicraft;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.loot.LootDataType;
import net.minecraft.util.Identifier;

public interface PSLootTableEntryType {
    static void bootstrap() {
        Map<Identifier, Identifier> extentionTableIds = new HashMap<>();
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, sources) -> {
            final boolean isVillagerChest = id.getPath().contains("village");
            if ((isVillagerChest || Psychedelicraft.getConfig().worldGeneration.get().villageChests())
            || (!isVillagerChest || Psychedelicraft.getConfig().worldGeneration.get().dungeonChests())) {
                if (Psychedelicraft.VANILLA_EXTENSIONS_NAMESPACE.equalsIgnoreCase(id.getNamespace())) {
                    extentionTableIds.put(new Identifier(id.getPath()), id);
                }
            }
        });
        LootTableEvents.ALL_LOADED.register((resourceManager, lootManager) -> {
            extentionTableIds.forEach((base, extra) -> {
                lootManager.getElementOptional(LootDataType.LOOT_TABLES, base).ifPresent(table -> {
                    lootManager.getElementOptional(LootDataType.LOOT_TABLES, extra).ifPresent(extraTable -> {
                        table.pools = Stream.concat(table.pools.stream(), extraTable.pools.stream()).toList();
                    });
                });
            });
            extentionTableIds.clear();
        });
    }
}
