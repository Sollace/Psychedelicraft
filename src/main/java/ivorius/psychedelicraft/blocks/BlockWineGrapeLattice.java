/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.blocks;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.items.PSItems;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Random;

public class BlockWineGrapeLattice extends Block implements IGrowable
{
    public BlockWineGrapeLattice(Settings settings) {
        super(settings);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack heldItem)
    {
        int dir = MathHelper.floor_double((entityLivingBase.rotationYaw * 4F) / 360F + 0.5D) & 3;

        switch (dir)
        {
            case 0:
                world.setBlockMetadataWithNotify(x, y, z, 0, 3);
                break;
            case 1:
                world.setBlockMetadataWithNotify(x, y, z, 1, 3);
                break;
            case 2:
                world.setBlockMetadataWithNotify(x, y, z, 0, 3);
                break;
            case 3:
                world.setBlockMetadataWithNotify(x, y, z, 1, 3);
                break;
        }

        super.onBlockPlacedBy(world, x, y, z, entityLivingBase, heldItem);
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int i, int j, int k)
    {
        setBlockBoundsBasedOnState(world, i, j, k);
        return super.getSelectedBoundingBoxFromPool(world, i, j, k);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4)
    {
        setBlockBoundsBasedOnState(par1World, par2, par3, par4);
        return super.getCollisionBoundingBoxFromPool(par1World, par2, par3, par4);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess iblockaccess, int i, int j, int k)
    {
        int m = iblockaccess.getBlockMetadata(i, j, k);

        float d = 0.1F;
        if ((m & 1) == 0)
        {
            setBlockBounds(0f, 0f, 0.5f - d, 1f, 1f, 0.5f + d);
        }
        else
        {
            setBlockBounds(0.5f - d, 0f, 0F, 0.5F + d, 1f, 1f);
        }
    }

    @Override
    public void setBlockBoundsForItemRender()
    {
        float d = 0.1F;
        setBlockBounds(0.5F - d, 0f, 0f, 0.5f + d, 1f, 1f);
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
    {
        ArrayList<ItemStack> drops = super.getDrops(world, x, y, z, metadata, fortune);

        if (metadata >> 1 == 4)
            drops.add(new ItemStack(PSItems.wineGrapes, world.rand.nextInt(3) + 1));

        return drops;
    }

    @Override
    public void harvestBlock(World world, EntityPlayer entityplayer, int x, int y, int z, int meta)
    {
        if (!world.isRemote && meta >> 1 > 0 && entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().getItem() == Items.shears)
        {
            if (meta >> 1 == 4)
                dropBlockAsItem(world, x, y, z, new ItemStack(PSItems.wineGrapes, world.rand.nextInt(3) + 1));

            world.setBlock(x, y, z, this, (meta & 1 | 2 << 1), 3);

            entityplayer.getCurrentEquippedItem().damageItem(1, entityplayer);
        }
        else
        {
            super.harvestBlock(world, entityplayer, x, y, z, meta);
        }
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random)
    {
        super.updateTick(world, x, y, z, random);

        if (world.getBlockLightValue(x, y + 1, z) >= 9)
        {
            if (this.func_149851_a(world, x, y, z, world.isRemote) && random.nextInt(35) == 0)
                this.growStep(world, random, x, y, z, false);
        }
    }

    public void growStep(World world, Random random, int x, int y, int z, boolean bonemeal)
    {
        if (!bonemeal || random.nextInt(2) == 0)
        {
            int m = world.getBlockMetadata(x, y, z);

            int d = m & 1;
            int g = m >> 1;

            if (g < 4 && g > 0)
            {
                g++;

                world.setBlockMetadataWithNotify(x, y, z, d | (g << 1), 3);
            }
        }
    }

    @Override
    public boolean func_149851_a(World world, int x, int y, int z, boolean isRemote)
    {
        int meta = world.getBlockMetadata(x, y, z);

        int d = meta & 1;
        int g = meta >> 1;

        if (g < 4 && g > 0)
        {
            return true;
        }

        return false;
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
}
