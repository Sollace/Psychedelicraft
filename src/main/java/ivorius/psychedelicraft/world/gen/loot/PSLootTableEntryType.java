package ivorius.psychedelicraft.world.gen.loot;

import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.Psychedelicraft;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public interface PSLootTableEntryType {
    LootPoolEntryType LOOT_TABLE_EXTENSION = register("loot_table_extension", LootExtensionEntry.CODEC);


    private static LootPoolEntryType register(String id, MapCodec<? extends LootPoolEntry> codec) {
        return Registry.register(Registries.LOOT_POOL_ENTRY_TYPE, Psychedelicraft.id(id), new LootPoolEntryType(codec));
    }

    static void bootstrap() {
        LootTableEvents.MODIFY.register((key, supplier, source) -> {
            Identifier id = key.getValue();
            if (!"minecraft".contentEquals(id.getNamespace())) {
                return;
            }
            // TODO: Check if table exists?
            final boolean isVillagerChest = id.getPath().contains("village");
            if ((isVillagerChest || Psychedelicraft.getConfig().balancing.worldGeneration.villageChests())
            || (!isVillagerChest || Psychedelicraft.getConfig().balancing.worldGeneration.dungeonChests())) {
                supplier.pool(LootPool.builder()
                        .with(LootExtensionEntry.builder(RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of("psychedelicraftmc", id.getPath())))));
            }
        });
    }
}
