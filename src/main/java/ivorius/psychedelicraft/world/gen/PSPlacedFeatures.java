package ivorius.psychedelicraft.world.gen;

import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.gen.feature.PlacedFeature;

public interface PSPlacedFeatures {
    RegistryKey<PlacedFeature> SPARCE_JUNIPER_TREE = of("sparce_juniper_tree_checked");
    RegistryKey<PlacedFeature> DENSE_JUNIPER_TREE = of("dense_juniper_tree_checked");
    RegistryKey<PlacedFeature> CANNABIS_TILLED_PATCH = of("cannabis_tilled_patch_checked");
    RegistryKey<PlacedFeature> HOP_TILLED_PATCH = of("hop_tilled_patch_checked");
    RegistryKey<PlacedFeature> TOBACCO_TILLED_PATCH = of("tobacco_tilled_patch_checked");
    RegistryKey<PlacedFeature> COFFEA_TILLED_PATCH = of("coffea_tilled_patch_checked");
    RegistryKey<PlacedFeature> COCA_TILLED_PATCH = of("coca_tilled_patch_checked");

    RegistryKey<PlacedFeature> MORNING_GLORY_PATCH_CHECKED = of("morning_glory_patch_checked");
    RegistryKey<PlacedFeature> BELLADONNA_PATCH_CHECKED = of("belladonna_patch_checked");
    RegistryKey<PlacedFeature> JIMSONWEED_PATCH_CHECKED = of("jimsonweed_patch_checked");
    RegistryKey<PlacedFeature> TOMATO_PATCH_CHECKED = of("tomato_patch_checked");
    RegistryKey<PlacedFeature> PEYOTE_PATCH_CHECKED = of("peyote_patch_checked");
    RegistryKey<PlacedFeature> AGAVE_PATCH_CHECKED = of("agave_patch_checked");

    RegistryKey<PlacedFeature> MORNING_GLORY_PATCH_UNCHECKED = of("morning_glory_patch_unchecked");
    RegistryKey<PlacedFeature> BELLADONNA_PATCH_UNCHECKED = of("belladonna_patch_unchecked");
    RegistryKey<PlacedFeature> JIMSONWEED_PATCH_UNCHECKED = of("jimsonweed_patch_unchecked");
    RegistryKey<PlacedFeature> TOMATO_PATCH_UNCHECKED = of("tomato_patch_unchecked");
    RegistryKey<PlacedFeature> PEYOTE_PATCH_UNCHECKED = of("peyote_patch_unchecked");
    RegistryKey<PlacedFeature> AGAVE_PATCH_UNCHECKED = of("agave_patch_unchecked");

    static RegistryKey<PlacedFeature> of(String id) {
        return RegistryKey.of(RegistryKeys.PLACED_FEATURE, Psychedelicraft.id(id));
    }

}
