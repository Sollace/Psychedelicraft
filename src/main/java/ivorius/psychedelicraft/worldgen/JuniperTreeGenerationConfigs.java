package ivorius.psychedelicraft.worldgen;

import java.util.List;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.blocks.PSBlocks;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.fabricmc.fabric.api.event.registry.DynamicRegistryView;
import net.minecraft.block.Blocks;
import net.minecraft.registry.*;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.size.TwoLayersFeatureSize;
import net.minecraft.world.gen.foliage.*;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.trunk.BendingTrunkPlacer;

public class JuniperTreeGenerationConfigs {
    public static final RegistryKey<ConfiguredFeature<?, ?>> JUNIPER_TREE_CONFIG = config("juniper_tree");
    public static final RegistryKey<PlacedFeature> JUNIPER_TREE_PLACEMENT = placement("juniper_tree_checked");

    private static void bootstrapConfigurations(Registry<ConfiguredFeature<?, ?>> registry) {
        var feature = new ConfiguredFeature<>(Feature.TREE, new TreeFeatureConfig.Builder(
                BlockStateProvider.of(PSBlocks.psycheLog),
                new BendingTrunkPlacer(5, 3, 3, 0, UniformIntProvider.create(-1, 1)),
                BlockStateProvider.of(PSBlocks.psycheLeaves),
                new BlobFoliagePlacer(
                        ConstantIntProvider.create(0),
                        ConstantIntProvider.ZERO,
                        0
                ),
                new TwoLayersFeatureSize(1, 0, 1))
        .dirtProvider(BlockStateProvider.of(Blocks.ROOTED_DIRT))
        .forceDirt()
        .build());

        Registry.register(registry, JUNIPER_TREE_CONFIG.getValue(), feature);
    }
    private static void bootstrapPlacements(DynamicRegistryView registries, Registry<PlacedFeature> registry) {
        var registryEntryLookup = registries.getOptional(RegistryKeys.CONFIGURED_FEATURE).orElseThrow();
        var placement = new PlacedFeature(registryEntryLookup.getEntry(JUNIPER_TREE_CONFIG).orElseThrow(), List.of(PlacedFeatures.wouldSurvive(PSBlocks.psycheSapling)));
        Registry.register(registry, JUNIPER_TREE_PLACEMENT.getValue(), placement);
    }

    public static RegistryKey<ConfiguredFeature<?, ?>> config(String id) {
        return RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, Psychedelicraft.id(id));
    }

    public static RegistryKey<PlacedFeature> placement(String id) {
        return RegistryKey.of(RegistryKeys.PLACED_FEATURE, Psychedelicraft.id(id));
    }

    static void bootstrap() {
        DynamicRegistrySetupCallback.EVENT.register(registries -> {
            registries.getOptional(RegistryKeys.CONFIGURED_FEATURE).ifPresent(registry -> {
                bootstrapConfigurations(registry);
            });
            registries.getOptional(RegistryKeys.PLACED_FEATURE).ifPresent(registry -> {
                bootstrapPlacements(registries, registry);
            });
        });
    }
}
