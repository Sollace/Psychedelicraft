/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.world.gen;

import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.ConfiguredFeature;

public class JuniperTreeSaplingGenerator extends SaplingGenerator {
    @Override
    protected RegistryEntry<ConfiguredFeature<?, ?>> getTreeFeature(Random random, boolean bees) {
        return PSWorldGen.JUNIPER_TREE_CONFIG;
    }
}
