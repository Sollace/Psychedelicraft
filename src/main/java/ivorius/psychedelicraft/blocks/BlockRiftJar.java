/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.blocks;

import ivorius.psychedelicraft.block.entity.TileEntityRiftJar;
import ivorius.psychedelicraft.items.ItemRiftJar;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.Random;

public class BlockRiftJar extends Block
{
    public BlockRiftJar()
    {
        super(Material.clay);

        setStepSound(soundTypeStone);
        setBlockBounds(0.1f, 0.1f, 0.1f, 0.9f, 0.8f, 0.9f);
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock()
    {
        return false;
    }

    @Override
    public int getRenderType()
    {
        return -1;
    }

    @Override
    public Item getItemDropped(int p_149650_1_, Random p_149650_2_, int p_149650_3_)
    {
        return null;
    }

    @Override
    public void breakBlock(World par1World, int par2, int par3, int par4, Block p_149749_5_, int p_149749_6_)
    {
        if (!par1World.isRemote)
        {
            TileEntity tileEntity = par1World.getTileEntity(par2, par3, par4);

            if (tileEntity != null && tileEntity instanceof TileEntityRiftJar)
            {
                TileEntityRiftJar tileEntityRiftJar = (TileEntityRiftJar) tileEntity;

                if (!tileEntityRiftJar.jarBroken)
                {
                    this.dropBlockAsItem(par1World, par2, par3, par4, ItemRiftJar.createFilledRiftJar(tileEntityRiftJar.currentRiftFraction, Item.getItemFromBlock(this)));
                }
            }
        }

        super.breakBlock(par1World, par2, par3, par4, p_149749_5_, p_149749_6_);
    }

    @Override
    public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9)
    {
        TileEntity tileEntity = par1World.getTileEntity(par2, par3, par4);
        if (tileEntity != null && tileEntity instanceof TileEntityRiftJar)
        {
            TileEntityRiftJar tileEntityRiftJar = (TileEntityRiftJar) tileEntity;

            if (par5EntityPlayer.isSneaking())
            {
                tileEntityRiftJar.toggleSuckingRifts();
            }
            else
            {
                tileEntityRiftJar.toggleRiftJarOpen();
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean hasTileEntity(int metadata)
    {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World var1, int var2)
    {
        return new TileEntityRiftJar();
    }
}
