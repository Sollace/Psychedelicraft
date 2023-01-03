/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.blocks;

import ivorius.psychedelicraft.block.entity.PeyoteBlockEntity;
import ivorius.psychedelicraft.items.PSItems;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;

import java.util.ArrayList;
import java.util.Random;

public class BlockPeyote extends BlockBush implements IGrowable
{
    public BlockPeyote()
    {
        super(Material.plants);
        float var3 = 0.2F;
        this.setBlockBounds(0.5F - var3, 0.0F, 0.5F - var3, 0.5F + var3, var3 * 2.0F, 0.5F + var3);
        this.setStepSound(soundTypeGrass);
    }

    @Override
    public int getRenderType()
    {
        return -1;
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random)
    {
        super.updateTick(world, x, y, z, random); // Ticks about every minute

        int meta = world.getBlockMetadata(x, y, z);

        if (meta < 3)
        {
            if (func_149851_a(world, x, y, z, world.isRemote) && random.nextInt(20) == 0)
                this.growStep(world, random, x, y, z, false);
        }
        else
        {
            if (func_149851_a(world, x, y, z, world.isRemote) && random.nextInt(120) == 0)
                this.growStep(world, random, x, y, z, false);
        }
    }

    @Override
    public boolean hasTileEntity(int metadata)
    {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World var1, int var2)
    {
        return new PeyoteBlockEntity();
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int meta, int fortune)
    {
        ArrayList<ItemStack> drops = new ArrayList<>();

        for (int i = 0; i < meta + 1; i++)
            drops.add(new ItemStack(PSBlocks.peyote, 1, 0));

        return drops;
    }

    public void growStep(World world, Random random, int x, int y, int z, boolean bonemeal)
    {
        int meta = world.getBlockMetadata(x, y, z);

        if (meta >= 3)
        {
            byte var6 = 4;
            int var8;
            int var9;
            int var10;

            var8 = x + random.nextInt(3) - 1;
            var9 = y + random.nextInt(2) - random.nextInt(2);
            var10 = z + random.nextInt(3) - 1;

            for (int var11 = 0; var11 < 4; ++var11)
            {
                if (world.isAirBlock(var8, var9, var10) && this.canBlockStay(world, var8, var9, var10))
                {
                    x = var8;
                    y = var9;
                    z = var10;
                }

                var8 = x + random.nextInt(3) - 1;
                var9 = y + random.nextInt(2) - random.nextInt(2);
                var10 = z + random.nextInt(3) - 1;
            }

            if (world.isAirBlock(var8, var9, var10) && this.canBlockStay(world, var8, var9, var10))
            {
                world.setBlock(var8, var9, var10, this, 0, 3);
            }
        }
        else
        {
            world.setBlockMetadataWithNotify(x, y, z, meta + 1, 3);
        }
    }

    @Override
    public boolean func_149851_a(World world, int x, int y, int z, boolean isRemote)
    {
        return true;
    }

    @Override
    public boolean func_149852_a(World world, Random random, int x, int y, int z)
    {
        // shouldGrow
        return true;
    }

    @Override
    public void func_149853_b(World world, Random random, int x, int y, int z)
    {
        // grow
        growStep(world, random, x, y, z, true);
    }

    @Override
    protected boolean canPlaceBlockOn(Block block)
    {
        return block.isNormalCube();
    }

    @Override
    public EnumPlantType getPlantType(IBlockAccess world, int x, int y, int z)
    {
        return EnumPlantType.Desert;
    }
}
