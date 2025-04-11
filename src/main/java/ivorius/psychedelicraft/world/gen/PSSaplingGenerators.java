package ivorius.psychedelicraft.world.gen;

import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.ConfiguredFeature;

public interface PSSaplingGenerators {
    SaplingGenerator JUNIPER = new SaplingGenerator() {
        @Override
        protected RegistryKey<ConfiguredFeature<?, ?>> getTreeFeature(Random random, boolean bees) {
            return PSFeatureConfigs.JUNIPER_TREE;
        }
    };
}
