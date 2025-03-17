package ivorius.psychedelicraft.world.gen;

import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.gen.feature.Feature;

public interface PSFeatures {
    RegistryKey<Feature<?>> TILLED_PATCH = RegistryKey.of(RegistryKeys.FEATURE, Psychedelicraft.id("tilled_patch"));
}
