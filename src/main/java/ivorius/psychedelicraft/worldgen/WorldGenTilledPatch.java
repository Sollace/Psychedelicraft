/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.worldgen;

import ivorius.psychedelicraft.blocks.IvTilledFieldPlant;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

public class WorldGenTilledPatch extends WorldGenerator
{
    public boolean needsWater;
    public Block block;

    public WorldGenTilledPatch(boolean notify, boolean needsWater, Block block)
    {
        super(notify);

        this.needsWater = needsWater;
        this.block = block;
    }

    @Override
    public boolean generate(World par1World, Random par2Random, int par3, int par4, int par5)
    {

        if (par1World.isAirBlock(par3, par4, par5) && par1World.getBlock(par3, par4 - 1, par5) == Blocks.grass && (!needsWater || isWaterNearby(par1World, par3, par4, par5)))
        {
            int range = par2Random.nextInt(3) + 1;

            for (int xPlus = -range; xPlus <= range; xPlus++)
            {
                for (int zPlus = -range; zPlus <= range; zPlus++)
                {
                    if (xPlus * xPlus + zPlus * zPlus < range * range && par1World.isAirBlock(par3 + xPlus, par4, par5 + zPlus) && par1World.getBlock(par3 + xPlus, par4 - 1, par5 + zPlus) == Blocks.grass)
                    {
                        if (par2Random.nextInt(3) == 0)
                        {
                            if (needsWater)
                            {
                                setBlockAndNotifyAdequately(par1World, par3 + xPlus, par4 - 1, par5 + zPlus, Blocks.farmland, needsWater ? 7 : 0);
                            }
                            else
                            {
                                setBlockAndNotifyAdequately(par1World, par3 + xPlus, par4 - 1, par5 + zPlus, Blocks.grass, 0);
                            }

                            int var10 = 2 + par2Random.nextInt(par2Random.nextInt(3) + 1);

                            for (int var11 = 0; var11 < var10; ++var11)
                            {
                                if (block.canBlockStay(par1World, par3 + xPlus, par4 + var11, par5 + zPlus))
                                {
                                    int meta = 0;

                                    if (block instanceof IvTilledFieldPlant)
                                    {
                                        meta = ((IvTilledFieldPlant) block).getMaxMetadata(var11);
                                    }

                                    if (meta >= 0)
                                    {
                                        setBlockAndNotifyAdequately(par1World, par3 + xPlus, par4 + var11, par5 + zPlus, block, meta);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return true;
        }

        return false;
    }

    public static boolean isWaterNearby(World world, int x, int y, int z)
    {
        for (int xP = -1; xP <= 1; xP++)
        {
            for (int zP = -1; zP <= 1; zP++)
            {
                if (world.getBlock(x + xP, y - 1, z + zP).getMaterial() == Material.water)
                {
                    return true;
                }
            }
        }

        return false;
    }
}
