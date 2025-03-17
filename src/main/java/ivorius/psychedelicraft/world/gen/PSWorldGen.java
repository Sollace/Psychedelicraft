/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.world.gen;

import java.util.function.Function;
import java.util.function.Predicate;

import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.config.BiomeSelector;
import ivorius.psychedelicraft.config.FeatureCustomConfig;
import ivorius.psychedelicraft.config.Generation;
import ivorius.psychedelicraft.world.gen.loot.PSLootTableEntryType;
import ivorius.psychedelicraft.world.gen.structure.MutableStructurePool;
import net.fabricmc.fabric.api.biome.v1.*;
import net.minecraft.registry.*;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.*;

/**
 * Created by lukas on 25.04.14.
 * Updated by Sollace on 16 Jan 2023
 */
public interface PSWorldGen {
    TilledPatchFeature TILLED_PATCH = Registry.register(Registries.FEATURE, PSFeatures.TILLED_PATCH, new TilledPatchFeature());

    static void bootstrap() {
        Predicate<BiomeSelectionContext> patchSpawnValid = BiomeSelectors.foundInOverworld().and(
                BiomeSelector.COLD
                .or(BiomeSelectors.tag(BiomeTags.IS_HILL))
                .or(BiomeSelectors.tag(BiomeTags.IS_FOREST))
                .or(BiomeSelectors.includeByKey(BiomeKeys.PLAINS))
        );
        plant(PSPlacedFeatures.DENSE_JUNIPER_TREE, PSTags.Biomes.HAS_DENSE_JUNIPER_TREES, Generation::juniper);
        plant(PSPlacedFeatures.SPARCE_JUNIPER_TREE, PSTags.Biomes.HAS_SPARCE_JUNIPER_TREES, Generation::juniper);
        plant(PSPlacedFeatures.MORNING_GLORY_PATCH_CHECKED, PSTags.Biomes.HAS_MORNING_GLORY, Generation::morningGlories);
        plant(PSPlacedFeatures.BELLADONNA_PATCH_CHECKED, PSTags.Biomes.HAS_BELLADONNA, Generation::belladonna);
        plant(PSPlacedFeatures.JIMSONWEED_PATCH_CHECKED, PSTags.Biomes.HAS_JIMSONWEED, Generation::jimsonweed);
        plant(PSPlacedFeatures.TOMATO_PATCH_CHECKED, PSTags.Biomes.HAS_TOMATOES, Generation::tomato);
        plant(PSPlacedFeatures.PEYOTE_PATCH_CHECKED, PSTags.Biomes.HAS_PEYOTE, Generation::peyote);
        plant(PSPlacedFeatures.AGAVE_PATCH_CHECKED, PSTags.Biomes.HAS_PEYOTE, Generation::peyote);
        plant(PSPlacedFeatures.CANNABIS_TILLED_PATCH, patchSpawnValid, Generation::cannabis);
        plant(PSPlacedFeatures.HOP_TILLED_PATCH, patchSpawnValid, Generation::hop);
        plant(PSPlacedFeatures.TOBACCO_TILLED_PATCH, patchSpawnValid, Generation::tobacco);
        plant(PSPlacedFeatures.COFFEA_TILLED_PATCH, patchSpawnValid, Generation::coffea);
        plant(PSPlacedFeatures.COCA_TILLED_PATCH, patchSpawnValid, Generation::coca);

        MutableStructurePool.bootstrap();
        PSLootTableEntryType.bootstrap();
    }

    private static void plant(RegistryKey<PlacedFeature> key, TagKey<Biome> biomes, Function<Generation, FeatureCustomConfig> configKey) {
        plant(key, BiomeSelectors.tag(biomes), configKey);
    }

    private static void plant(RegistryKey<PlacedFeature> key, Predicate<BiomeSelectionContext> predicate, Function<Generation, FeatureCustomConfig> configKey) {
        BiomeModifications.addFeature(context -> {
            var config = configKey.apply(Psychedelicraft.getConfig().worldGeneration.get());
            return config.enabled() && config.spawnableBiomes().createPredicate(predicate).test(context);
        }, GenerationStep.Feature.VEGETAL_DECORATION, key);
    }
}
