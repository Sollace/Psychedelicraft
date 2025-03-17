package ivorius.psychedelicraft.datagen.providers.loot;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.BlockWithFluid;
import ivorius.psychedelicraft.block.BurdenedLatticeBlock;
import ivorius.psychedelicraft.block.CannabisPlantBlock;
import ivorius.psychedelicraft.block.CocaPlantBlock;
import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.block.PeyoteBlock;
import ivorius.psychedelicraft.block.TobaccoPlantBlock;
import ivorius.psychedelicraft.item.PSItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.BlockStatePropertyLootCondition;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.condition.SurvivesExplosionLootCondition;
import net.minecraft.loot.condition.TableBonusLootCondition;
import net.minecraft.loot.entry.DynamicEntry;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.ApplyBonusLootFunction;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.CopyStateLootFunction;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.Util;

public class PSBlockLootTableProvider extends FabricBlockLootTableProvider {
    private ConditionalLootFunction.Builder<?> fortuneBonus;

    public PSBlockLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        fortuneBonus = ApplyBonusLootFunction.binomialWithBonusCount(getEnchantment(Enchantments.FORTUNE), 0.5714286F, 3);
        // simple drops
        List.of(
                PSBlocks.DRYING_TABLE,
                PSBlocks.IRON_DRYING_TABLE,

                PSBlocks.JUNIPER_BUTTON,
                PSBlocks.JUNIPER_FENCE_GATE,
                PSBlocks.JUNIPER_FENCE,
                PSBlocks.JUNIPER_LOG, PSBlocks.JUNIPER_WOOD,
                PSBlocks.STRIPPED_JUNIPER_LOG, PSBlocks.STRIPPED_JUNIPER_WOOD,
                PSBlocks.JUNIPER_PLANKS,
                PSBlocks.JUNIPER_PRESSURE_PLATE,
                PSBlocks.JUNIPER_SAPLING,

                PSBlocks.JUNIPER_SIGN,
                PSBlocks.JUNIPER_HANGING_SIGN,

                PSBlocks.JUNIPER_STAIRS,
                PSBlocks.JUNIPER_TRAPDOOR,

                PSBlocks.LATTICE,
                PSBlocks.GLASS_TUBE,
                PSBlocks.GLASS_VALVE,
                PSBlocks.BUNSEN_BURNER,
                PSBlocks.BOTTLE_RACK
        ).forEach(this::addDrop);
        addDrop(PSBlocks.TRAY, block -> LootTable.builder().pool(
            LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1.0F))
                .with(DynamicEntry.builder(BlockWithFluid.CONTENTS_DYNAMIC_DROP_ID))
                .conditionally(SurvivesExplosionLootCondition.builder())
        ).pool(
            LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1.0F))
                .with(ItemEntry.builder(PSBlocks.TRAY))
                .conditionally(SurvivesExplosionLootCondition.builder())
        ));
        addDrop(PSBlocks.WALL_BOTTLE_RACK, PSItems.BOTTLE_RACK);

        addDrop(PSBlocks.JUNIPER_LEAVES, block -> fruitLeavesDrop(block, PSBlocks.JUNIPER_SAPLING, PSItems.JUNIPER_BERRIES, SAPLING_DROP_CHANCE));
        addDrop(PSBlocks.FRUITING_JUNIPER_LEAVES, block -> matureFruitLeavesDrop(block, PSBlocks.JUNIPER_SAPLING, PSItems.JUNIPER_BERRIES, SAPLING_DROP_CHANCE));
        addDrop(PSBlocks.JUNIPER_SLAB, this::slabDrops);
        addDrop(PSBlocks.JUNIPER_DOOR, this::doorDrops);

        // Potted things
        List.of(
                PSBlocks.POTTED_CANNABIS,
                PSBlocks.POTTED_COCA,
                PSBlocks.POTTED_COFFEA,
                PSBlocks.POTTED_HOP,
                PSBlocks.POTTED_JUNIPER_SAPLING,
                PSBlocks.POTTED_MORNING_GLORY,
                PSBlocks.POTTED_TOBACCO
        ).forEach(this::addPottedPlantDrops);

        // Fluid machines
        List.of(
                PSBlocks.MASH_TUB,
                PSBlocks.FLASK,
                PSBlocks.DISTILLERY,
                PSBlocks.PLACED_DRINK,
                PSBlocks.RIFT_JAR,
                PSBlocks.PALE_OAK_BARREL
        ).forEach(block -> addDrop(block, this::dynamicContentDrops));
        PSBlocks.ALL_BARRELS.forEach(block -> addDrop(block, this::dynamicContentDrops));

        addDrop(PSBlocks.HOP, block -> drugCropDrops(block, 11, Properties.AGE_15_MAX, PSItems.HOP_CONES, PSItems.HOP_SEEDS));
        addDrop(PSBlocks.COCA, block -> drugCropDrops(block, 5, CocaPlantBlock.AGE_12_MAX, PSItems.COCA_LEAVES, PSItems.COCA_SEEDS));
        addDrop(PSBlocks.CANNABIS, block -> drugCropDrops(block, 5, Properties.AGE_15_MAX, PSItems.CANNABIS_LEAF, PSItems.CANNABIS_SEEDS)
                .pool(cropDrops(block, Properties.AGE_15_MAX, PSItems.CANNABIS_BUDS)));
        addDrop(PSBlocks.COFFEA, block -> singleItemDrugCropDrops(block, 1, Properties.AGE_7_MAX, PSItems.COFFEA_CHERRIES));
        addDrop(PSBlocks.TOBACCO, block -> tieredDrugCropDrops(block, 1, Properties.AGE_7_MAX, PSItems.TOBACCO_LEAVES, PSItems.TOBACCO_SEEDS));
        addDrop(PSBlocks.PEYOTE, this::peyoteDrops);
        addDrop(PSBlocks.MORNING_GLORY, block -> morningGloryDrops(block, PSItems.MORNING_GLORY, PSItems.MORNING_GLORY_SEEDS));

        addDrop(PSBlocks.MORNING_GLORY_LATTICE, block -> latticeCropDrops(block, PSItems.MORNING_GLORY_SEEDS, PSItems.MORNING_GLORY_SEEDS));
        addLatticeShearingDrops(PSBlocks.MORNING_GLORY_LATTICE, PSItems.MORNING_GLORY);
        addDrop(PSBlocks.WINE_GRAPE_LATTICE, block -> latticeCropDrops(block, PSItems.WINE_GRAPES, PSItems.WINE_GRAPES));
        addLatticeShearingDrops(PSBlocks.WINE_GRAPE_LATTICE, PSItems.WINE_GRAPES);

        addDrop(PSBlocks.TOMATOES, PSItems.TOMATO_SEEDS);
        addDrop(PSBlocks.JIMSONWEED, PSItems.JIMSONWEED_SEEDS);
        addDrop(PSBlocks.BELLADONNA, PSItems.BELLADONNA_SEEDS);

        // Empty drops
        List.of(
                PSBlocks.AGAVE_PLANT,
                PSBlocks.MASH_TUB_EDGE
        ).forEach(block -> addDrop(block, dropsNothing()));

        // check for missing blocks
        Registries.BLOCK.forEach(block -> {
            var key = block.getLootTableKey();
            if (key.getValue().getNamespace().equalsIgnoreCase("psychedelicraft") && !lootTables.containsKey(key)) {
                Psychedelicraft.LOGGER.warn("No loot table provided for " + key);
            }
            if (block instanceof BurdenedLatticeBlock b) {
                if (!lootTables.containsKey(b.getFarmingLootTableKey())) {
                    Psychedelicraft.LOGGER.warn("No loot table provided for " + key);
                }
            }
        });
    }

    private LootTable.Builder singleItemDrugCropDrops(Block block, int middleAge, int maturityAge, Item product) {
        return LootTable.builder()
                .pool(cropProductDrops(block, middleAge, product))
                .pool(upperCropDrops(block, maturityAge, product));
    }

    private LootTable.Builder drugCropDrops(Block block, int middleAge, int maturityAge, Item product, Item seeds) {
        return LootTable.builder()
                .pool(intermediateCropDrops(block, middleAge, product, seeds))
                .pool(cropDrops(block, maturityAge, seeds));
    }

    private LootTable.Builder tieredDrugCropDrops(Block block, int middleAge, int maturityAge, Item product, Item seeds) {
        return LootTable.builder()
            .pool(intermediateCropDrops(block, middleAge, product, seeds))
            .pool(upperCropDrops(block, maturityAge, seeds))            ;
    }

    private LootPool.Builder upperCropDrops(Block block, int maturityAge, Item product) {
        return LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1))
                .conditionally(BlockStatePropertyLootCondition.builder(block)
                        .properties(StatePredicate.Builder.create()
                                .exactMatch(PeyoteBlock.AGE, maturityAge)
                                .exactMatch(TobaccoPlantBlock.TOP, true)
                        ))
                .with(ItemEntry.builder(product).apply(fortuneBonus))
                .conditionally(SurvivesExplosionLootCondition.builder());
    }

    private LootTable.Builder morningGloryDrops(Block block, Item product, Item seeds) {
        return LootTable.builder().pool(LootPool.builder()
                .conditionally(SurvivesExplosionLootCondition.builder())
                .rolls(ConstantLootNumberProvider.create(1))
                .with(ItemEntry.builder(product)
                        .conditionally(BlockStatePropertyLootCondition.builder(block)
                                .properties(StatePredicate.Builder.create()
                                        .exactMatch(Properties.AGE_4, Properties.AGE_4_MAX)
                                ))
                        .conditionally(TableBonusLootCondition.builder(getEnchantment(Enchantments.FORTUNE),0.2F, 0.3F, 0.4F, 0.5F, 0.6F))
                        .alternatively(ItemEntry.builder(seeds).apply(fortuneBonus))
                ).conditionally(SurvivesExplosionLootCondition.builder()));
    }

    private LootPool.Builder cropDrops(Block block, int maturityAge, Item product) {
        return LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1))
                .conditionally(BlockStatePropertyLootCondition.builder(block)
                        .properties(StatePredicate.Builder.create()
                                .exactMatch(PeyoteBlock.AGE, maturityAge)
                        ))
                .with(ItemEntry.builder(product).apply(fortuneBonus))
                .conditionally(SurvivesExplosionLootCondition.builder());
    }

    private LootPool.Builder intermediateCropDrops(Block block, int maturityAge, Item product, Item seeds) {
        return LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1))
                .with(ItemEntry.builder(product)
                        .conditionally(rangedStateCondition(block, ((CannabisPlantBlock)block).getAgeProperty(), maturityAge, null))
                        .apply(fortuneBonus)
                        .alternatively(ItemEntry.builder(seeds)))
                .conditionally(SurvivesExplosionLootCondition.builder());
    }

    private LootPool.Builder cropProductDrops(Block block, int maturityAge, Item product) {
        return LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1))
                .with(ItemEntry.builder(product)
                        .conditionally(rangedStateCondition(block, ((CannabisPlantBlock)block).getAgeProperty(), maturityAge, null))
                        .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(3, 4))))
                .conditionally(SurvivesExplosionLootCondition.builder());
    }

    private LootTable.Builder peyoteDrops(Block block) {
        return LootTable.builder()
                .pool(addSurvivesExplosionCondition(block, Util.make(LootPool.builder()
                    .rolls(ConstantLootNumberProvider.create(1)), pool -> PeyoteBlock.AGE.getValues().forEach(age -> {
                        pool.with(ItemEntry.builder(block)
                            .apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(age + 1)))
                            .conditionally(BlockStatePropertyLootCondition.builder(block)
                            .properties(StatePredicate.Builder.create().exactMatch(PeyoteBlock.AGE, age))));
                    })).conditionally(SurvivesExplosionLootCondition.builder())));
    }

    public LootTable.Builder fruitLeavesDrop(Block leaves, Block sapling, Item fruit, float... saplingChance) {
        return leavesDrops(leaves, sapling, saplingChance)
            .pool(
                LootPool.builder()
                    .rolls(ConstantLootNumberProvider.create(1.0F))
                    .conditionally(createWithoutShearsOrSilkTouchCondition())
                    .with(addSurvivesExplosionCondition(leaves, ItemEntry.builder(fruit))
                        .conditionally(TableBonusLootCondition.builder(getEnchantment(Enchantments.FORTUNE),
                                0.005F, 0.0055555557F, 0.00625F, 0.008333334F, 0.025F
                        ))
                    )
            );
    }

    public LootTable.Builder matureFruitLeavesDrop(Block leaves, Block sapling, Item fruit, float... saplingChance) {
        return leavesDrops(leaves, sapling, saplingChance)
            .pool(
                LootPool.builder()
                    .rolls(ConstantLootNumberProvider.create(1.0F))
                    .conditionally(createWithoutShearsOrSilkTouchCondition())
                    .with(addSurvivesExplosionCondition(leaves, ItemEntry.builder(fruit)).apply(fortuneBonus))
            );
    }

    public LootTable.Builder latticeCropDrops(Block block, Item product, Item seeds) {
        return LootTable.builder()
                .pool(LootPool.builder()
                        .conditionally(createSilkTouchCondition())
                        .rolls(ConstantLootNumberProvider.create(1.0F))
                        .with(addSurvivesExplosionCondition(block, ItemEntry.builder(block)).apply(CopyStateLootFunction.builder(block).addProperty(Properties.AGE_3))))
                .pool(LootPool.builder()
                        .conditionally(createWithoutSilkTouchCondition())
                        .rolls(ConstantLootNumberProvider.create(1.0F))
                        .with(addSurvivesExplosionCondition(product, ItemEntry.builder(product))
                                .conditionally(BlockStatePropertyLootCondition.builder(block)
                                        .properties(StatePredicate.Builder.create()
                                                .exactMatch(Properties.AGE_3, Properties.AGE_3_MAX)
                                        ))
                                .alternatively(ItemEntry.builder(seeds))
                        ))
                .pool(LootPool.builder()
                        .conditionally(createWithoutSilkTouchCondition())
                        .rolls(ConstantLootNumberProvider.create(1.0F))
                        .with(addSurvivesExplosionCondition(block, ItemEntry.builder(PSBlocks.LATTICE))));
    }

    public void addLatticeShearingDrops(Block block, Item product) {
        this.lootTables.put(((BurdenedLatticeBlock)block).getFarmingLootTableKey(), LootTable.builder()
                .pool(LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1.0F))
                        .with(addSurvivesExplosionCondition(product, ItemEntry.builder(product))
                                .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(3, 4)))
                                .conditionally(BlockStatePropertyLootCondition.builder(block)
                                        .properties(StatePredicate.Builder.create()
                                                .exactMatch(Properties.AGE_3, Properties.AGE_3_MAX)
                                        ))
                        )));
    }

    private RegistryEntry<Enchantment> getEnchantment(RegistryKey<Enchantment> key) {
        return registryLookup.getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(key);
    }

    @SuppressWarnings("deprecation")
    static <T extends Comparable<T>> LootCondition.Builder rangedStateCondition(Block block, Property<T> property, @Nullable T min, @Nullable T max) {
        return () -> JsonOps.INSTANCE.getMap(Util.make(new JsonObject(), json -> {
            json.addProperty("block", block.getRegistryEntry().getIdAsString());
            json.addProperty("condition", Registries.LOOT_CONDITION_TYPE.getId(LootConditionTypes.BLOCK_STATE_PROPERTY).toString());
            json.add("properties", Util.make(new JsonObject(), o -> {
                o.add(property.getName(), Util.make(new JsonObject(), oo -> {
                    if (min != null) oo.addProperty("min", String.valueOf(min));
                    if (max != null) oo.addProperty("max", String.valueOf(max));
                }));
            }));
        })).flatMap(map -> BlockStatePropertyLootCondition.CODEC.decode(JsonOps.INSTANCE, map)).getOrThrow();
    }

    private LootTable.Builder dynamicContentDrops(Block block) {
        return LootTable.builder().pool(
            LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1.0F))
                .with(DynamicEntry.builder(BlockWithFluid.CONTENTS_DYNAMIC_DROP_ID))
                .conditionally(SurvivesExplosionLootCondition.builder())
        );
    }
}
