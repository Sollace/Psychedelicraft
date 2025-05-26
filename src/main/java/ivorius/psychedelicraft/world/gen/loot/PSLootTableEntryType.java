package ivorius.psychedelicraft.world.gen.loot;

import java.util.List;

import ivorius.psychedelicraft.Psychedelicraft;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.loot.LootDataType;
import net.minecraft.util.Identifier;

public interface PSLootTableEntryType {
    static void bootstrap() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, sources) -> {
            final boolean isVillagerChest = id.getPath().contains("village");
            if ((isVillagerChest || Psychedelicraft.getConfig().worldGeneration.get().villageChests())
            || (!isVillagerChest || Psychedelicraft.getConfig().worldGeneration.get().dungeonChests())) {
                if (Identifier.DEFAULT_NAMESPACE.equals(id.getNamespace())) {
                    lootManager.getElementOptional(LootDataType.LOOT_TABLES, new Identifier(Psychedelicraft.VANILLA_EXTENSIONS_NAMESPACE, id.getPath())).ifPresent(extraTable -> {
                        List.of(extraTable.pools).forEach(tableBuilder::pool);
                    });
                }
            }
        });
    }
}
