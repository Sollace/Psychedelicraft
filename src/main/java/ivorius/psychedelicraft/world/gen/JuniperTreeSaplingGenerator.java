/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.world.gen;

import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.ConfiguredFeature;

public class JuniperTreeSaplingGenerator extends SaplingGenerator {
    @Override
    protected RegistryKey<ConfiguredFeature<?, ?>> getTreeFeature(Random random, boolean bees) {
        return JuniperTreeGenerationConfigs.JUNIPER_TREE_CONFIG;
    }
/*
    @Override
    public boolean generate(World world, Random random, int x, int y, int z) {
        int trunkHeight = random.nextInt(3) + 5;

        if (this.isGiant) {
            trunkHeight += random.nextInt(7);
        }

        boolean flag = true;

        if (y >= 1 && y + trunkHeight + 1 <= 256) {
            for (int i1 = y; i1 <= y + 1 + trunkHeight; ++i1) {
                byte b0 = 1;

                if (i1 == y) {
                    b0 = 0;
                }

                if (i1 >= y + 1 + trunkHeight - 2) {
                    b0 = 2;
                }

                for (int j1 = x - b0; j1 <= x + b0 && flag; ++j1) {
                    for (int k1 = z - b0; k1 <= z + b0 && flag; ++k1) {
                        if (i1 >= 0 && i1 < 256) {
                            Block block = world.getBlock(j1, i1, k1);

                            if (!this.isReplaceable(world, j1, i1, k1)) {
                                flag = false;
                            }
                        } else {
                            flag = false;
                        }
                    }
                }
            }

            if (!flag) {
                return false;
            }

            Block block2 = world.getBlock(x, y - 1, z);

            boolean isSoil = block2.canSustainPlant(world, x, y - 1, z, Direction.UP, Blocks.OAK_SAPLING);
            if (isSoil && y < 256 - trunkHeight - 1) {
                placeTree(world, random, block2, x, y, z, trunkHeight);
                return true;
            }

            return false;
        }

        return false;
    }

    private void placeTree(World world, Random random, Block block2, int x, int y, int z, int trunkHeight) {
        block2.onPlantGrow(world, x, y - 1, z, x, y, z);
        int trunkXOffset = 0;
        int trunkZOffset = 0;

        BlockPos.Mutable pos = new BlockPos.Mutable();

        // generate trunk
        // 1. Normal 1x1 trunk
        // 2. Height is of 3 to 5 blocks
        // 3. Trunk has random chance of shifting on the x/z direction
        for (int trunkY = 0; trunkY < trunkHeight; ++trunkY) {
            pos.set(x + trunkXOffset, y + trunkY, z + trunkZOffset);

            if (world.isAir(pos) || world.getBlockState(pos).isIn(BlockTags.REPLACEABLE_PLANTS)) {
                world.setBlockState(pos, PSBlocks.psycheLog.getDefaultState());
            }

            if (random.nextBoolean()) {
                trunkXOffset += random.nextInt(3) - 1;
            } else {
                trunkZOffset += random.nextInt(3) - 1;
            }
        }

        // generate leaves
        // 1. leaves blob is 3 blocks tall
        // 2. with corners cut out
        // 3.
/*
        for (int leavesY = y + trunkHeight - 3; leavesY <= y + trunkHeight; leavesY++) {
            int canopyY = leavesY - (y + trunkHeight);
            int leavesWidth = 1 - canopyY / 2;

            for (int leavesX = x - leavesWidth; leavesX <= x + leavesWidth; leavesX++) {
                int l1 = leavesX - x;

                for (int leavesZ = z - leavesWidth; leavesZ <= z + leavesWidth; leavesZ++) {
                    int j2 = leavesZ - z;

                    pos.set(leavesX + trunkXOffset, canopyY, leavesZ + trunkZOffset);

                    if (Math.abs(l1) != leavesWidth || Math.abs(j2) != leavesWidth || random.nextInt(2) != 0 && canopyY != 0) {
                        if (world.isAir(pos) || world.getBlockState(pos).isIn(BlockTags.REPLACEABLE_PLANTS)) {
                            world.setBlockState(pos, PSBlocks.psycheLeaves.getDefaultState());
                        }
                    }
                }
            }
        }\* /
    }*/
}
