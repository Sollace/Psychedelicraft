/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.worldgen;

import ivorius.psychedelicraft.blocks.PSBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.ConfiguredFeature;

public class WorldGenJuniperTrees extends SaplingGenerator
{
    public boolean field_150531_a;

    public WorldGenJuniperTrees(boolean par1)
    {
        super(par1);

        field_150531_a = false;
    }

    @Override
    protected RegistryKey<ConfiguredFeature<?, ?>> getTreeFeature(Random random, boolean bees) {
        return null;
    }

    @Override
    public boolean generate(World par1World, Random par2Random, int par3, int par4, int par5)
    {
        int l = par2Random.nextInt(3) + 5;

        if (this.field_150531_a)
        {
            l += par2Random.nextInt(7);
        }

        boolean flag = true;

        if (par4 >= 1 && par4 + l + 1 <= 256)
        {
            int j1;
            int k1;

            for (int i1 = par4; i1 <= par4 + 1 + l; ++i1)
            {
                byte b0 = 1;

                if (i1 == par4)
                {
                    b0 = 0;
                }

                if (i1 >= par4 + 1 + l - 2)
                {
                    b0 = 2;
                }

                for (j1 = par3 - b0; j1 <= par3 + b0 && flag; ++j1)
                {
                    for (k1 = par5 - b0; k1 <= par5 + b0 && flag; ++k1)
                    {
                        if (i1 >= 0 && i1 < 256)
                        {
                            Block block = par1World.getBlock(j1, i1, k1);

                            if (!this.isReplaceable(par1World, j1, i1, k1))
                            {
                                flag = false;
                            }
                        }
                        else
                        {
                            flag = false;
                        }
                    }
                }
            }

            if (!flag)
            {
                return false;
            }
            else
            {
                Block block2 = par1World.getBlock(par3, par4 - 1, par5);

                boolean isSoil = block2.canSustainPlant(par1World, par3, par4 - 1, par5, ForgeDirection.UP, (BlockSapling) Blocks.sapling);
                if (isSoil && par4 < 256 - l - 1)
                {
                    block2.onPlantGrow(par1World, par3, par4 - 1, par5, par3, par4, par5);
                    int k2;

                    int xP = 0;
                    int zP = 0;

                    for (k2 = 0; k2 < l; ++k2)
                    {
                        int x = par3 + xP;
                        int z = par5 + zP;

                        Block block3 = par1World.getBlock(x, par4 + k2, z);

                        if (block3.isAir(par1World, x, par4 + k2, z) || block3.isLeaves(par1World, x, par4 + k2, z))
                        {
                            this.setBlockAndNotifyAdequately(par1World, x, par4 + k2, z, PSBlocks.psycheLog, 0);
                        }

                        if (par2Random.nextBoolean())
                        {
                            xP += par2Random.nextInt(3) - 1;
                        }
                        else
                        {
                            zP += par2Random.nextInt(3) - 1;
                        }
                    }

                    for (k2 = par4 - 3 + l; k2 <= par4 + l; ++k2)
                    {
                        j1 = k2 - (par4 + l);
                        k1 = 1 - j1 / 2;

                        for (int l2 = par3 - k1; l2 <= par3 + k1; ++l2)
                        {
                            int l1 = l2 - par3;

                            for (int i2 = par5 - k1; i2 <= par5 + k1; ++i2)
                            {
                                int j2 = i2 - par5;

                                int x = l2 + xP;
                                int z = i2 + zP;

                                if (Math.abs(l1) != k1 || Math.abs(j2) != k1 || par2Random.nextInt(2) != 0 && j1 != 0)
                                {
                                    Block block1 = par1World.getBlock(x, k2, z);

                                    if (block1.isAir(par1World, x, k2, z) || block1.isLeaves(par1World, x, k2, z))
                                    {
                                        this.setBlockAndNotifyAdequately(par1World, x, k2, z, PSBlocks.psycheLeaves, 0);
                                    }
                                }
                            }
                        }
                    }

                    return true;
                }
                else
                {
                    return false;
                }
            }
        }
        else
        {
            return false;
        }
    }
}
