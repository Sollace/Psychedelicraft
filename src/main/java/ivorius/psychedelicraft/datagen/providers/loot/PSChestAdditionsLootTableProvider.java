package ivorius.psychedelicraft.datagen.providers.loot;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.fluid.AlcoholicFluid;
import ivorius.psychedelicraft.fluid.CoffeeFluid;
import ivorius.psychedelicraft.fluid.PSFluids;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.world.gen.loot.SetFluidsLootFunction;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.LootTable.Builder;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.TagEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
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
        acceptAdditions((id, builder) -> exporter.accept(RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(Psychedelicraft.VANILLA_EXTENSIONS_NAMESPACE, id.getValue().getPath())), builder));
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
                .with(loot(PSItems.STONE_CUP, 3, 1, 1).apply(SetFluidsLootFunction.builder(PSFluids.COFFEE).attribute(CoffeeFluid.WARMTH, ConstantLootNumberProvider.create(2))))
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
                .with(loot(PSItems.STONE_CUP, 3, 1, 1).apply(SetFluidsLootFunction.builder(PSFluids.COFFEE)))
                .with(ItemEntry.builder(PSItems.STONE_CUP))
                .with(loot(PSItems.HASH_MUFFIN, 1, 1, 8))
        ));
        exporter.accept(LootTables.VILLAGE_TOOLSMITH_CHEST, LootTable.builder().pool(LootPool.builder()
                .rolls(UniformLootNumberProvider.create(3, 8))
                .with(loot(PSItems.WOODEN_MUG, 10, 1, 3))
                .with(loot(PSItems.CIGARETTE, 1, 1, 16))
                .with(loot(PSItems.CIGAR, 2, 1, 4))
                .with(loot(PSItems.JOINT, 1, 1, 1))
                .with(loot(PSItems.STONE_CUP, 1, 1, 1).apply(SetFluidsLootFunction.builder(PSFluids.COFFEE).attribute(CoffeeFluid.WARMTH, ConstantLootNumberProvider.create(2))))
                .with(loot(PSItems.STONE_CUP, 1, 1, 1).apply(SetFluidsLootFunction.builder(PSFluids.PEYOTE_JUICE)))
                .with(loot(PSItems.SYRINGE, 1, 1, 1).apply(SetFluidsLootFunction.builder(Set.of(PSFluids.COCAINE, PSFluids.CAFFEINE))))
                .with(loot(PSItems.HASH_MUFFIN, 1, 1, 8))
        ));
        exporter.accept(LootTables.VILLAGE_WEAPONSMITH_CHEST, LootTable.builder().pool(LootPool.builder()
                .rolls(UniformLootNumberProvider.create(3, 8))
                .with(loot(PSItems.WOODEN_MUG, 3, 1, 16))
                .with(loot(PSItems.CIGARETTE, 1, 1, 16))
                .with(loot(PSItems.CIGAR, 2, 1, 2))
                .with(loot(PSItems.STONE_CUP, 1, 1, 1).apply(SetFluidsLootFunction.builder(PSFluids.COFFEE).attribute(CoffeeFluid.WARMTH, ConstantLootNumberProvider.create(2))))
                .with(loot(PSItems.HASH_MUFFIN, 3, 1, 8))
        ));
        exporter.accept(LootTables.SHIPWRECK_SUPPLY_CHEST, LootTable.builder()
                .pool(LootPool.builder()
                    .rolls(UniformLootNumberProvider.create(1, 2))
                    .with(loot(PSItems.WOODEN_MUG, 1, 1, 3).apply(SetFluidsLootFunction.builder(PSFluids.SUGAR_CANE)
                            .attribute(AlcoholicFluid.FERMENTATION, ConstantLootNumberProvider.create(2))
                            .attribute(AlcoholicFluid.DISTILLATION, UniformLootNumberProvider.create(8, 16))
                    ))
                    .with(loot(PSItems.WOODEN_MUG, 1, 1, 3).apply(SetFluidsLootFunction.builder(PSFluids.SUGAR_CANE)
                            .attribute(AlcoholicFluid.FERMENTATION, ConstantLootNumberProvider.create(2))
                            .attribute(AlcoholicFluid.DISTILLATION, UniformLootNumberProvider.create(8, 16))
                            .attribute(AlcoholicFluid.MATURATION, UniformLootNumberProvider.create(8, 16))
                    ))
                )
                .pool(LootPool.builder()
                    .rolls(UniformLootNumberProvider.create(0, 1)).apply(SetFluidsLootFunction.builder(Set.of(PSFluids.SUGAR_CANE, PSFluids.RED_GRAPES))
                            .attribute(AlcoholicFluid.FERMENTATION, ConstantLootNumberProvider.create(2))
                            .attribute(AlcoholicFluid.DISTILLATION, UniformLootNumberProvider.create(8, 16))
                            .attribute(AlcoholicFluid.MATURATION, UniformLootNumberProvider.create(8, 16))
                    )
                    .with(loot(PSTags.Items.BARRELS, 1, 1, 1))
                    .with(loot(PSTags.Items.BOTTLE_RACK_INSERTABLE, 1, 1, 1))
        ));
        exporter.accept(LootTables.ANCIENT_CITY_ICE_BOX_CHEST, LootTable.builder().pool(LootPool.builder()
                .rolls(UniformLootNumberProvider.create(2, 4))
                .with(ItemEntry.builder(Items.BUCKET).apply(SetFluidsLootFunction.builder(PSFluids.MILK)
                        .attribute(AlcoholicFluid.FERMENTATION, ConstantLootNumberProvider.create(2))
                ))
        ));
    }

    static ItemEntry.Builder<?> loot(ItemConvertible item, int weight, int minCount, int maxCount) {
        return ItemEntry.builder(item).weight(weight).apply(
                SetCountLootFunction.builder(minCount == maxCount ? ConstantLootNumberProvider.create(minCount) : UniformLootNumberProvider.create(minCount, maxCount))
        );
    }

    static TagEntry.Builder<?> loot(TagKey<Item> tag, int weight, int minCount, int maxCount) {
        return TagEntry.expandBuilder(tag).weight(weight).apply(
                SetCountLootFunction.builder(minCount == maxCount ? ConstantLootNumberProvider.create(minCount) : UniformLootNumberProvider.create(minCount, maxCount))
        );
    }
}
