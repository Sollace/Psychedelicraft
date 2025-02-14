package ivorius.psychedelicraft.datagen.providers.tag;

import java.util.concurrent.CompletableFuture;

import ivorius.psychedelicraft.PSTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

public class PSBiomeTagProvider extends FabricTagProvider<Biome> {

    public PSBiomeTagProvider(FabricDataOutput output, CompletableFuture<WrapperLookup> completableFuture) {
        super(output, RegistryKeys.BIOME, completableFuture);
    }

    @Override
    protected void configure(WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(PSTags.Biomes.HAS_SPARCE_JUNIPER_TREES)
            .forceAddTag(BiomeTags.IS_HILL);
        getOrCreateTagBuilder(PSTags.Biomes.HAS_DENSE_JUNIPER_TREES)
            .forceAddTag(BiomeTags.IS_FOREST)
            .add(BiomeKeys.WINDSWEPT_FOREST);
        getOrCreateTagBuilder(PSTags.Biomes.HAS_MORNING_GLORY)
            .add(BiomeKeys.FLOWER_FOREST)
            .add(BiomeKeys.SUNFLOWER_PLAINS)
            .add(BiomeKeys.MEADOW)
            .add(BiomeKeys.LUSH_CAVES);
        getOrCreateTagBuilder(PSTags.Biomes.HAS_BELLADONNA)
            .add(BiomeKeys.DARK_FOREST)
            .add(BiomeKeys.FLOWER_FOREST);
        getOrCreateTagBuilder(PSTags.Biomes.HAS_JIMSONWEED)
            .add(BiomeKeys.JUNGLE)
            .add(BiomeKeys.BAMBOO_JUNGLE)
            .add(BiomeKeys.SPARSE_JUNGLE);
        getOrCreateTagBuilder(PSTags.Biomes.HAS_TOMATOES)
            .forceAddTag(BiomeTags.IS_FOREST);
        getOrCreateTagBuilder(PSTags.Biomes.HAS_PEYOTE)
            .forceAddTag(BiomeTags.IS_SAVANNA)
            .forceAddTag(BiomeTags.IS_BADLANDS)
            .forceAddTag(BiomeTags.DESERT_PYRAMID_HAS_STRUCTURE);
    }
}
