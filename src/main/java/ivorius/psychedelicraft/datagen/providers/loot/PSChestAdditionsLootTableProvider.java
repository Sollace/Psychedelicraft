package ivorius.psychedelicraft.datagen.providers.loot;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.fluid.PSFluids;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.PSComponents;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.minecraft.item.ItemConvertible;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.LootTable.Builder;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetComponentsLootFunction;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

public class PSChestAdditionsLootTableProvider extends SimpleFabricLootTableProvider {
    public PSChestAdditionsLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup, LootContextTypes.CHEST);
    }

    @Override
    public String getName() {
        return super.getName() + " Additions";
    }

    @Override
    public void accept(BiConsumer<RegistryKey<LootTable>, LootTable.Builder> exporter) {
        acceptAdditions((id, builder) -> exporter.accept(RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of("psychedelicraftmc", id.getValue().getPath())), builder));
    }

    public void acceptAdditions(BiConsumer<RegistryKey<LootTable>, Builder> exporter) {
        exporter.accept(LootTables.ABANDONED_MINESHAFT_CHEST, LootTable.builder().pool(LootPool.builder()
                .with(loot(PSItems.WINE_GRAPES, 8, 3, 8))
                .with(loot(PSItems.CIGARETTE, 5, 1, 8))
                .with(loot(PSItems.SMOKING_PIPE, 3, 1, 1))
                .with(loot(PSItems.WOODEN_MUG, 5, 2, 16))
                .with(loot(PSItems.JUNIPER_BERRIES, 2, 1, 8))
                .with(loot(PSItems.DRIED_TOBACCO, 6, 1, 16))
        ));
        exporter.accept(LootTables.SIMPLE_DUNGEON_CHEST, LootTable.builder().pool(LootPool.builder()
                .with(loot(PSItems.WINE_GRAPES, 8, 1, 8))
                .with(loot(PSItems.GLASS_CHALICE, 5, 2, 4))
                .with(loot(PSItems.WOODEN_MUG, 2, 1, 16))
                .with(loot(PSItems.JUNIPER_BERRIES, 10, 1, 8))
                .with(loot(PSItems.DRIED_TOBACCO, 3, 1, 16))
        ));
        exporter.accept(LootTables.PILLAGER_OUTPOST_CHEST, LootTable.builder().pool(LootPool.builder()
                .with(loot(PSItems.BONG, 8, 1, 8))
                .with(loot(PSItems.SMOKING_PIPE, 5, 2, 4))
                .with(loot(PSItems.STONE_CUP, 2, 1, 16))
                .with(loot(PSItems.DRIED_COCA_LEAVES, 10, 1, 8))
                .with(loot(PSItems.DRIED_TOBACCO, 3, 1, 16))
        ));
        exporter.accept(LootTables.DESERT_PYRAMID_CHEST, LootTable.builder().pool(LootPool.builder()
                .with(loot(PSItems.PEYOTE_JOINT, 8, 1, 8))
                .with(loot(PSItems.DRIED_PEYOTE, 5, 2, 4))
                .with(loot(PSItems.AGAVE_LEAF, 2, 1, 16))
        ));
        exporter.accept(LootTables.VILLAGE_SHEPARD_CHEST, LootTable.builder().pool(LootPool.builder()
                .rolls(UniformLootNumberProvider.create(3, 8))
                .with(loot(PSItems.WOODEN_MUG, 1, 1, 16))
                .with(loot(PSItems.CIGARETTE, 1, 1, 16))
                .with(loot(PSItems.WOODEN_MUG, 3, 1, 1).apply(SetComponentsLootFunction.builder(PSComponents.FLUIDS, PSFluids.COFFEE.getDefaultStack(FluidVolumes.MUG).withAttribute("warmth", 2))))
        ));
        exporter.accept(LootTables.VILLAGE_TANNERY_CHEST, LootTable.builder().pool(LootPool.builder()
                .rolls(UniformLootNumberProvider.create(3, 8))
                .with(loot(PSItems.CIGARETTE, 4, 1, 11))
        ));
        exporter.accept(LootTables.VILLAGE_TEMPLE_CHEST, LootTable.builder().pool(LootPool.builder()
                .rolls(UniformLootNumberProvider.create(3, 8))
                .with(loot(PSItems.WINE_GRAPES, 8, 3, 10))
                .with(loot(PSItems.WOODEN_MUG, 1, 1, 16))
                .with(loot(PSItems.CIGARETTE, 2, 1, 16))
                .with(loot(PSItems.JOINT, 2, 1, 16))
                .with(loot(PSItems.WOODEN_MUG, 3, 1, 1).apply(SetComponentsLootFunction.builder(PSComponents.FLUIDS, PSFluids.COFFEE.getDefaultStack(FluidVolumes.MUG))))
                .with(ItemEntry.builder(PSItems.STONE_CUP))
                .with(loot(PSItems.HASH_MUFFIN, 1, 1, 8))
        ));
        exporter.accept(LootTables.VILLAGE_TOOLSMITH_CHEST, LootTable.builder().pool(LootPool.builder()
                .rolls(UniformLootNumberProvider.create(3, 8))
                .with(loot(PSItems.WOODEN_MUG, 10, 1, 3))
                .with(loot(PSItems.CIGARETTE, 1, 1, 16))
                .with(loot(PSItems.CIGAR, 2, 1, 4))
                .with(loot(PSItems.JOINT, 1, 1, 1))
                .with(loot(PSItems.WOODEN_MUG, 1, 1, 1).apply(SetComponentsLootFunction.builder(PSComponents.FLUIDS, PSFluids.COFFEE.getDefaultStack(FluidVolumes.MUG).withAttribute("warmth", 2))))
                .with(loot(PSItems.STONE_CUP, 1, 1, 1).apply(SetComponentsLootFunction.builder(PSComponents.FLUIDS, PSFluids.PEYOTE_JUICE.getDefaultStack(FluidVolumes.CUP))))
                .with(loot(PSItems.SYRINGE, 1, 1, 1).apply(SetComponentsLootFunction.builder(PSComponents.FLUIDS, PSFluids.COCAINE.getDefaultStack(FluidVolumes.SYRINGE))))
                .with(loot(PSItems.SYRINGE, 1, 1, 1).apply(SetComponentsLootFunction.builder(PSComponents.FLUIDS, PSFluids.CAFFEINE.getDefaultStack(FluidVolumes.SYRINGE))))
                .with(loot(PSItems.HASH_MUFFIN, 1, 1, 8))
        ));
        exporter.accept(LootTables.VILLAGE_WEAPONSMITH_CHEST, LootTable.builder().pool(LootPool.builder()
                .rolls(UniformLootNumberProvider.create(3, 8))
                .with(loot(PSItems.WOODEN_MUG, 3, 1, 16))
                .with(loot(PSItems.CIGARETTE, 1, 1, 16))
                .with(loot(PSItems.CIGAR, 2, 1, 2))
                .with(loot(PSItems.WOODEN_MUG, 1, 1, 1).apply(SetComponentsLootFunction.builder(PSComponents.FLUIDS, PSFluids.COFFEE.getDefaultStack(FluidVolumes.MUG).withAttribute("warmth", 2))))
                .with(loot(PSItems.HASH_MUFFIN, 3, 1, 8))
        ));
    }

    static ItemEntry.Builder<?> loot(ItemConvertible item, int weight, int minCount, int maxCount) {
        return ItemEntry.builder(item).weight(weight).apply(
                SetCountLootFunction.builder(minCount == maxCount ? ConstantLootNumberProvider.create(minCount) : UniformLootNumberProvider.create(minCount, maxCount))
        );
    }

}
