/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.world.gen;

import java.util.List;
import java.util.function.Predicate;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.CannabisPlantBlock;
import ivorius.psychedelicraft.block.PSBlocks;
import net.fabricmc.fabric.api.biome.v1.*;
import net.minecraft.block.Blocks;
import net.minecraft.registry.*;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.size.TwoLayersFeatureSize;
import net.minecraft.world.gen.foliage.BlobFoliagePlacer;
import net.minecraft.world.gen.placementmodifier.*;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.trunk.ForkingTrunkPlacer;

/**
 * Created by lukas on 25.04.14.
 * Updated by Sollace on 16 Jan 2023
 */
public class PSWorldGen {
    public static final TilledPatchFeature TILLED_PATCH_FEATURE = Registry.register(Registries.FEATURE, Psychedelicraft.id("tilled_patch"), new TilledPatchFeature());

    public static final RegistryKey<ConfiguredFeature<?, ?>> JUNIPER_TREE_CONFIG = createConfiguredFeature("juniper_tree");
    public static final RegistryKey<PlacedFeature> JUNIPER_TREE_PLACEMENT = createPlacement("juniper_tree_checked");

    private static final Predicate<BiomeSelectionContext> IS_COLD = ctx -> {
        return ctx.getBiome().getPrecipitation() == Biome.Precipitation.SNOW;
    };

    private static final Predicate<BiomeSelectionContext> IS_DRY = ctx -> {
        return ctx.getBiome().getPrecipitation() == Biome.Precipitation.NONE;
    };

    public static RegistryKey<ConfiguredFeature<?, ?>> createConfiguredFeature(String name) {
        return RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, Psychedelicraft.id(name));
    }

    public static RegistryKey<PlacedFeature> createPlacement(String id) {
        return RegistryKey.of(RegistryKeys.PLACED_FEATURE, Psychedelicraft.id(id));
    }

    private static void registerFilledPatch(String id, CannabisPlantBlock crop, boolean requireWater) {
        var cannabisPatch = createConfiguredFeature(id + "_tilled_patch");
        FeatureRegistry.registerConfiguredFeature(cannabisPatch, () -> {
            return new ConfiguredFeature<>(TILLED_PATCH_FEATURE, new TilledPatchFeature.Config(requireWater, crop));
        });

        var placement = createPlacement(id + "_tilled_patch_checked");
        FeatureRegistry.registerPlacedFeature(placement, cannabisPatch, feature -> {
            return new PlacedFeature(feature, List.of(RarityFilterPlacementModifier.of(1), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of()));
        });

        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld().and(
                        IS_COLD
                        .or(BiomeSelectors.tag(BiomeTags.IS_HILL))
                        .or(BiomeSelectors.tag(BiomeTags.IS_FOREST))
                        .or(ctx -> ctx.getBiomeKey() == BiomeKeys.PLAINS)
                ),
                GenerationStep.Feature.VEGETAL_DECORATION,
                placement
        );
    }

    public static void bootstrap() {
        FeatureRegistry.registerConfiguredFeature(JUNIPER_TREE_CONFIG, () -> {
            return new ConfiguredFeature<>(Feature.TREE, new TreeFeatureConfig.Builder(
                    BlockStateProvider.of(PSBlocks.JUNIPER_LOG),
                    new ForkingTrunkPlacer(5, 2, 2),
                    BlockStateProvider.of(PSBlocks.JUNIPER_LEAVES),
                    new BlobFoliagePlacer(
                            ConstantIntProvider.create(2),
                            ConstantIntProvider.ZERO,
                            3
                    ),
                    new TwoLayersFeatureSize(1, 0, 2))
            .dirtProvider(BlockStateProvider.of(Blocks.ROOTED_DIRT))
            .forceDirt()
            .build());
        });
        FeatureRegistry.registerPlacedFeature(JUNIPER_TREE_PLACEMENT, JUNIPER_TREE_CONFIG, config -> {
            return new PlacedFeature(config, VegetationPlacedFeatures.modifiersWithWouldSurvive(
                    PlacedFeatures.createCountExtraModifier(1, 0.05F, 2),
                    PSBlocks.JUNIPER_SAPLING)
            );
        });

        if (Psychedelicraft.getConfig().balancing.worldGeneration.genJuniper) {
            BiomeModifications.addFeature(
                    BiomeSelectors.foundInOverworld().and(IS_COLD).and(
                        BiomeSelectors.tag(BiomeTags.IS_HILL).or(BiomeSelectors.tag(BiomeTags.IS_FOREST))
                    ),
                    GenerationStep.Feature.VEGETAL_DECORATION,
                    JUNIPER_TREE_PLACEMENT
            );
        }

        if (Psychedelicraft.getConfig().balancing.worldGeneration.genCannabis) {
            registerFilledPatch("cannabis", PSBlocks.CANNABIS, false);
        }

        if (Psychedelicraft.getConfig().balancing.worldGeneration.genHop) {
            registerFilledPatch("hop", PSBlocks.HOP, false);
        }

        if (Psychedelicraft.getConfig().balancing.worldGeneration.genTobacco) {
            registerFilledPatch("tobacco", PSBlocks.TOBACCO, false);
        }

        if (Psychedelicraft.getConfig().balancing.worldGeneration.genCoffea) {
            registerFilledPatch("coffea", PSBlocks.COFFEA, false);
        }

        if (Psychedelicraft.getConfig().balancing.worldGeneration.genCoca) {
            registerFilledPatch("coca", PSBlocks.COCA, true);
        }

        if (Psychedelicraft.getConfig().balancing.worldGeneration.genPeyote) {
            var peyotePatch = createConfiguredFeature("peyote_patch");

            FeatureRegistry.registerConfiguredFeature(peyotePatch, () -> {
                return new ConfiguredFeature<>(Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(
                        1,
                        PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK,
                        new SimpleBlockFeatureConfig(BlockStateProvider.of(PSBlocks.PEYOTE)))
                ));
            });

            var placement = createPlacement("peyote_patch_checked");

            FeatureRegistry.registerPlacedFeature(placement, peyotePatch, feature -> {
                return new PlacedFeature(feature, List.of(
                        RarityFilterPlacementModifier.of(1),
                        SquarePlacementModifier.of(),
                        PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP,
                        BiomePlacementModifier.of()));
            });

            BiomeModifications.addFeature(
                    BiomeSelectors.foundInOverworld().and(
                        BiomeSelectors.tag(BiomeTags.IS_SAVANNA)
                        .or(BiomeSelectors.tag(BiomeTags.IS_BADLANDS))
                        .or(BiomeSelectors.tag(BiomeTags.DESERT_PYRAMID_HAS_STRUCTURE))
                        .or(IS_DRY)
                    ),
                    GenerationStep.Feature.VEGETAL_DECORATION,
                    placement
            );
        }
        /*
        if (PSConfig.dungeonChests)
        {
            // TODO: (Sollace) Loot tables
            addItem(DUNGEON_CHEST, new WeightedRandomChestContent(wineGrapes, 0, 1, 8, 10));
            addItem(DUNGEON_CHEST, new WeightedRandomChestContent(glassChalice, 0, 1, 4, 5));
            addItem(DUNGEON_CHEST, new WeightedRandomChestContent(woodenMug, 0, 1, 16, 5));
            addItem(DUNGEON_CHEST, new WeightedRandomChestContent(juniperBerries, 0, 1, 8, 10));
            addItem(DUNGEON_CHEST, new WeightedRandomChestContent(driedTobacco, 0, 1, 16, 5));

            addItem(MINESHAFT_CORRIDOR, new WeightedRandomChestContent(wineGrapes, 0, 1, 8, 10));
            addItem(MINESHAFT_CORRIDOR, new WeightedRandomChestContent(woodenMug, 0, 1, 16, 5));
            addItem(MINESHAFT_CORRIDOR, new WeightedRandomChestContent(juniperBerries, 0, 1, 8, 10));
            addItem(MINESHAFT_CORRIDOR, new WeightedRandomChestContent(driedTobacco, 0, 1, 16, 5));
            addItem(MINESHAFT_CORRIDOR, new WeightedRandomChestContent(pipe, 0, 1, 1, 3));
            addItem(MINESHAFT_CORRIDOR, new WeightedRandomChestContent(cigarette, 0, 1, 8, 5));
        }

        if (PSConfig.villageChests)
        {
            // TODO: (Sollace) Loot tables
            addItem(VILLAGE_BLACKSMITH, new WeightedRandomChestContent(wineGrapes, 0, 1, 8, 10));
            addItem(VILLAGE_BLACKSMITH, new WeightedRandomChestContent(juniperBerries, 0, 1, 8, 10));
            addItem(VILLAGE_BLACKSMITH, new WeightedRandomChestContent(woodenMug, 0, 1, 16, 5));
            addItem(VILLAGE_BLACKSMITH, new WeightedRandomChestContent(cigarette, 0, 1, 16, 1));
            addItem(VILLAGE_BLACKSMITH, new WeightedRandomChestContent(cigar, 0, 1, 1, 1));
            addItem(VILLAGE_BLACKSMITH, new WeightedRandomChestContent(joint, 0, 1, 1, 1));

            ItemStack coffeeStack = new ItemStack(woodenMug);
            woodenMug.fill(coffeeStack, new FluidStack(PSFluids.coffee, 1), true);
            addItem(VILLAGE_BLACKSMITH, new WeightedRandomChestContent(coffeeStack, 1, 4, 1));

            ItemStack peyoteStack = new ItemStack(stoneCup);
            stoneCup.fill(coffeeStack, new FluidStack(PSFluids.peyoteJuice, 1), true);
            addItem(VILLAGE_BLACKSMITH, new WeightedRandomChestContent(peyoteStack, 1, 1, 1));

            ItemStack cocaineStack = new ItemStack(syringe);
            syringe.fill(coffeeStack, new FluidStack(PSFluids.cocaineFluid, 1), true);
            addItem(VILLAGE_BLACKSMITH, new WeightedRandomChestContent(cocaineStack, 1, 1, 1));

            ItemStack caffeineStack = new ItemStack(syringe);
            syringe.fill(coffeeStack, new FluidStack(PSFluids.caffeineFluid, 1), true);
            addItem(VILLAGE_BLACKSMITH, new WeightedRandomChestContent(caffeineStack, 1, 1, 1));

            addItem(VILLAGE_BLACKSMITH, new WeightedRandomChestContent(hashMuffin, 0, 1, 8, 1));
        }*/
    }
}
