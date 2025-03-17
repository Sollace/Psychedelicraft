package ivorius.psychedelicraft.datagen;

import java.util.List;

import ivorius.psychedelicraft.block.AgavePlantBlock;
import ivorius.psychedelicraft.block.CannabisPlantBlock;
import ivorius.psychedelicraft.block.NightshadeBlock;
import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.block.PeyoteBlock;
import ivorius.psychedelicraft.block.VineStemBlock;
import ivorius.psychedelicraft.world.gen.PSFeatureConfigs;
import ivorius.psychedelicraft.world.gen.PSPlacedFeatures;
import ivorius.psychedelicraft.world.gen.PSWorldGen;
import ivorius.psychedelicraft.world.gen.TilledPatchFeature;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.ConfiguredFeatures;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.PlacedFeatures;
import net.minecraft.world.gen.feature.SimpleBlockFeatureConfig;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.feature.VegetationPlacedFeatures;
import net.minecraft.world.gen.feature.size.TwoLayersFeatureSize;
import net.minecraft.world.gen.foliage.BlobFoliagePlacer;
import net.minecraft.world.gen.placementmodifier.BiomePlacementModifier;
import net.minecraft.world.gen.placementmodifier.RarityFilterPlacementModifier;
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.stateprovider.RandomizedIntBlockStateProvider;
import net.minecraft.world.gen.trunk.ForkingTrunkPlacer;

final class PSWorldGenFeatures {
    static void bootstrapConfiguredFeatures(Registerable<ConfiguredFeature<?, ?>> registerable) {
        registerable.register(PSFeatureConfigs.JUNIPER_TREE, new ConfiguredFeature<>(Feature.TREE, new TreeFeatureConfig.Builder(
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
            .build()));
        registerable.register(PSFeatureConfigs.CANNABIS_TILLED_PATCH, createTilledPatch(PSBlocks.CANNABIS, false));
        registerable.register(PSFeatureConfigs.HOP_TILLED_PATCH, createTilledPatch(PSBlocks.HOP, false));
        registerable.register(PSFeatureConfigs.TOBACCO_TILLED_PATCH, createTilledPatch(PSBlocks.TOBACCO, false));
        registerable.register(PSFeatureConfigs.COFFEA_TILLED_PATCH, createTilledPatch(PSBlocks.COFFEA, false));
        registerable.register(PSFeatureConfigs.COCA_TILLED_PATCH, createTilledPatch(PSBlocks.COCA, true));
        registerable.register(PSFeatureConfigs.MORNING_GLORY_PATCH, createUnTilledPatch(PSBlocks.MORNING_GLORY, VineStemBlock.AGE, UniformIntProvider.create(0, VineStemBlock.MAX_AGE)));
        registerable.register(PSFeatureConfigs.BELLADONNA_PATCH, createUnTilledPatch(PSBlocks.BELLADONNA, NightshadeBlock.AGE, UniformIntProvider.create(0, NightshadeBlock.MAX_AGE)));
        registerable.register(PSFeatureConfigs.JIMSONWEED_PATCH, createUnTilledPatch(PSBlocks.JIMSONWEED, NightshadeBlock.AGE, UniformIntProvider.create(0, NightshadeBlock.MAX_AGE)));
        registerable.register(PSFeatureConfigs.TOMATO_PATCH, createUnTilledPatch(PSBlocks.TOMATOES, NightshadeBlock.AGE, UniformIntProvider.create(0, NightshadeBlock.MAX_AGE)));
        registerable.register(PSFeatureConfigs.PEYOTE_PATCH, createUnTilledPatch(PSBlocks.PEYOTE, PeyoteBlock.AGE, UniformIntProvider.create(0, PeyoteBlock.MAX_AGE)));
        registerable.register(PSFeatureConfigs.AGAVE_PATCH, createUnTilledPatch(PSBlocks.AGAVE_PLANT, AgavePlantBlock.AGE, UniformIntProvider.create(0, AgavePlantBlock.MAX_AGE)));
    }

    static void bootstrapPlacedFeatures(Registerable<PlacedFeature> registerable) {
        var features = registerable.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE);

        registerable.register(PSPlacedFeatures.DENSE_JUNIPER_TREE, new PlacedFeature(features.getOrThrow(PSFeatureConfigs.JUNIPER_TREE),
                VegetationPlacedFeatures.treeModifiersWithWouldSurvive(
                    PlacedFeatures.createCountExtraModifier(3, 0.05F, 2),
                    PSBlocks.JUNIPER_SAPLING)
        ));
        registerable.register(PSPlacedFeatures.SPARCE_JUNIPER_TREE, new PlacedFeature(features.getOrThrow(PSFeatureConfigs.JUNIPER_TREE),
                VegetationPlacedFeatures.treeModifiersWithWouldSurvive(
                    PlacedFeatures.createCountExtraModifier(1, 0.05F, 2),
                    PSBlocks.JUNIPER_SAPLING)
        ));
        registerable.register(PSPlacedFeatures.CANNABIS_TILLED_PATCH, createTilledPatchPlacement(features.getOrThrow(PSFeatureConfigs.CANNABIS_TILLED_PATCH)));
        registerable.register(PSPlacedFeatures.HOP_TILLED_PATCH, createTilledPatchPlacement(features.getOrThrow(PSFeatureConfigs.HOP_TILLED_PATCH)));
        registerable.register(PSPlacedFeatures.TOBACCO_TILLED_PATCH, createTilledPatchPlacement(features.getOrThrow(PSFeatureConfigs.TOBACCO_TILLED_PATCH)));
        registerable.register(PSPlacedFeatures.COFFEA_TILLED_PATCH, createTilledPatchPlacement(features.getOrThrow(PSFeatureConfigs.COFFEA_TILLED_PATCH)));
        registerable.register(PSPlacedFeatures.COCA_TILLED_PATCH, createTilledPatchPlacement(features.getOrThrow(PSFeatureConfigs.COCA_TILLED_PATCH)));

        registerUnTilledPatchPlacement(registerable, features.getOrThrow(PSFeatureConfigs.MORNING_GLORY_PATCH), PSPlacedFeatures.MORNING_GLORY_PATCH_CHECKED, PSPlacedFeatures.MORNING_GLORY_PATCH_UNCHECKED);
        registerUnTilledPatchPlacement(registerable, features.getOrThrow(PSFeatureConfigs.BELLADONNA_PATCH), PSPlacedFeatures.BELLADONNA_PATCH_CHECKED, PSPlacedFeatures.BELLADONNA_PATCH_UNCHECKED);
        registerUnTilledPatchPlacement(registerable, features.getOrThrow(PSFeatureConfigs.JIMSONWEED_PATCH), PSPlacedFeatures.JIMSONWEED_PATCH_CHECKED, PSPlacedFeatures.JIMSONWEED_PATCH_UNCHECKED);
        registerUnTilledPatchPlacement(registerable, features.getOrThrow(PSFeatureConfigs.TOMATO_PATCH), PSPlacedFeatures.TOMATO_PATCH_CHECKED, PSPlacedFeatures.TOMATO_PATCH_UNCHECKED);
        registerUnTilledPatchPlacement(registerable, features.getOrThrow(PSFeatureConfigs.PEYOTE_PATCH), PSPlacedFeatures.PEYOTE_PATCH_CHECKED, PSPlacedFeatures.PEYOTE_PATCH_UNCHECKED);
        registerUnTilledPatchPlacement(registerable, features.getOrThrow(PSFeatureConfigs.AGAVE_PATCH), PSPlacedFeatures.AGAVE_PATCH_CHECKED, PSPlacedFeatures.AGAVE_PATCH_UNCHECKED);
    }

    private static ConfiguredFeature<?, ?> createTilledPatch(CannabisPlantBlock crop, boolean requireWater) {
        return new ConfiguredFeature<>(PSWorldGen.TILLED_PATCH, new TilledPatchFeature.Config(requireWater, crop));
    }

    private static PlacedFeature createTilledPatchPlacement(RegistryEntry<ConfiguredFeature<?, ?>> feature) {
        return new PlacedFeature(feature, List.of(
                RarityFilterPlacementModifier.of(90),
                SquarePlacementModifier.of(),
                PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP,
                BiomePlacementModifier.of()
        ));
    }

    private static ConfiguredFeature<?, ?> createUnTilledPatch(Block plant, IntProperty ageProperty, IntProvider ageRange) {
        return new ConfiguredFeature<>(Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(
                5,
                PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK,
                new SimpleBlockFeatureConfig(new RandomizedIntBlockStateProvider(BlockStateProvider.of(plant), ageProperty, ageRange)))));
    }

    private static void registerUnTilledPatchPlacement(Registerable<PlacedFeature> registerable, RegistryEntry<ConfiguredFeature<?, ?>> feature, RegistryKey<PlacedFeature> checkedPlacementId, RegistryKey<PlacedFeature> uncheckedPlacementId) {
        registerable.register(checkedPlacementId, new PlacedFeature(feature, List.of(
                    RarityFilterPlacementModifier.of(20),
                    SquarePlacementModifier.of(),
                    PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP,
                    BiomePlacementModifier.of())));
        registerable.register(uncheckedPlacementId, new PlacedFeature(feature, List.of()));
    }
}
