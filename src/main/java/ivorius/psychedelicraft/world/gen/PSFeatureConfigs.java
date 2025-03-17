package ivorius.psychedelicraft.world.gen;

import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.gen.feature.ConfiguredFeature;

public interface PSFeatureConfigs {
    RegistryKey<ConfiguredFeature<?, ?>> JUNIPER_TREE = of("juniper_tree");
    RegistryKey<ConfiguredFeature<?, ?>> CANNABIS_TILLED_PATCH = of("cannabis_tilled_patch");
    RegistryKey<ConfiguredFeature<?, ?>> HOP_TILLED_PATCH = of("hop_tilled_patch");
    RegistryKey<ConfiguredFeature<?, ?>> TOBACCO_TILLED_PATCH = of("tobacco_tilled_patch");
    RegistryKey<ConfiguredFeature<?, ?>> COFFEA_TILLED_PATCH = of("coffea_tilled_patch");
    RegistryKey<ConfiguredFeature<?, ?>> COCA_TILLED_PATCH = of("coca_tilled_patch");

    RegistryKey<ConfiguredFeature<?, ?>> MORNING_GLORY_PATCH = of("morning_glory_patch");
    RegistryKey<ConfiguredFeature<?, ?>> BELLADONNA_PATCH = of("belladonna_patch");
    RegistryKey<ConfiguredFeature<?, ?>> JIMSONWEED_PATCH = of("jimsonweed_patch");
    RegistryKey<ConfiguredFeature<?, ?>> TOMATO_PATCH = of("tomato_patch");
    RegistryKey<ConfiguredFeature<?, ?>> PEYOTE_PATCH = of("peyote_patch");
    RegistryKey<ConfiguredFeature<?, ?>> AGAVE_PATCH = of("agave_patch");

    static RegistryKey<ConfiguredFeature<?, ?>> of(String name) {
        return RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, Psychedelicraft.id(name));
    }

}
