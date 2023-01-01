/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.blocks;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.screen.PSGuiHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

/**
 * Created by lukas on 25.10.14.
 */
public class BlockFlask extends Block
{
    public BlockFlask()
    {
        super(Material.glass);
        setBlockBounds(0.25f, 0.0f, 0.25f, 0.75f, 0.7f, 0.75f);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
    {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof TileEntityFlask)
        {
            if (!world.isRemote)
                player.openGui(Psychedelicraft.instance, PSGuiHandler.fluidHandlerContainerID_UP, world, x, y, z);

            return true;
        }

        return false;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack stack)
    {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof TileEntityFlask)
        {
            TileEntityFlask tileEntityFlask = (TileEntityFlask) tileEntity;

            FluidStack fluidStack = stack.getItem() instanceof IFluidContainerItem ? ((IFluidContainerItem) stack.getItem()).getFluid(stack) : null;
            if (fluidStack != null)
                tileEntityFlask.fill(ForgeDirection.UP, fluidStack, true);
        }
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest)
    {
        if (willHarvest)
        {
            TileEntity tileEntity = world.getTileEntity(x, y, z);
            if (tileEntity instanceof TileEntityFlask)
            {
                TileEntityFlask tileEntityFlask = (TileEntityFlask) tileEntity;
                FluidStack fluidStack = tileEntityFlask.drain(ForgeDirection.DOWN, TileEntityFlask.FLASK_CAPACITY, true);
                ItemStack stack = new ItemStack(this);

                if (fluidStack != null && fluidStack.amount > 0)
                    ((IFluidContainerItem) stack.getItem()).fill(stack, fluidStack, true);

                dropBlockAsItem(world, x, y, z, stack);
            }
        }

        return super.removedByPlayer(world, player, x, y, z, willHarvest);
    }

    @Override
    public void dropBlockAsItemWithChance(World p_149690_1_, int p_149690_2_, int p_149690_3_, int p_149690_4_, int p_149690_5_, float p_149690_6_, int p_149690_7_)
    {

    }

    @Override
    public void registerBlockIcons(IIconRegister p_149651_1_)
    {

    }

    @Override
    public IIcon getIcon(int side, int meta)
    {
        return Blocks.glass.getIcon(side, 0);
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
    public boolean hasTileEntity(int metadata)
    {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, int meta)
    {
        return new TileEntityFlask();
    }
}
