/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.blocks;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.screen.PSGuiHandler;
import ivorius.psychedelicraft.fluids.FluidHelper;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

public class BlockBarrel extends Block
{
    public static final int MAX_TAP_AMOUNT = FluidHelper.MILLIBUCKETS_PER_LITER;

    public BlockBarrel()
    {
        super(Material.wood);

        setStepSound(soundTypeWood);
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
    public IIcon getIcon(int par1, int par2)
    {
        return Blocks.planks.getIcon(0, 0);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack stack)
    {
        int direction = MathHelper.floor_double((entityLivingBase.rotationYaw * 4F) / 360F + 0.5D) & 3;
        world.setBlockMetadataWithNotify(x, y, z, direction, 3);

        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof TileEntityBarrel)
        {
            TileEntityBarrel tileEntityBarrel = (TileEntityBarrel) tileEntity;

            FluidStack fluidStack = stack.getItem() instanceof IFluidContainerItem ? ((IFluidContainerItem) stack.getItem()).getFluid(stack) : null;
            if (fluidStack != null)
                tileEntityBarrel.fill(ForgeDirection.UP, fluidStack, true);

            tileEntityBarrel.barrelWoodType = stack.getItemDamage();
        }
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest)
    {
        if (willHarvest)
        {
            TileEntity tileEntity = world.getTileEntity(x, y, z);
            if (tileEntity instanceof TileEntityBarrel)
            {
                TileEntityBarrel tileEntityBarrel = (TileEntityBarrel) tileEntity;
                FluidStack fluidStack = tileEntityBarrel.drain(ForgeDirection.DOWN, TileEntityBarrel.BARREL_CAPACITY, true);
                ItemStack stack = new ItemStack(this, 1, tileEntityBarrel.barrelWoodType);

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
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float par7, float par8, float par9)
    {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof TileEntityBarrel)
        {
            TileEntityBarrel tileEntityBarrel = (TileEntityBarrel) tileEntity;

            ItemStack heldItem = player.getHeldItem();

            if (heldItem != null && heldItem.getItem() instanceof IFluidContainerItem)
            {
                boolean split = heldItem.stackSize > 1;
                ItemStack stack = split ? heldItem.splitStack(1) : heldItem;

                IFluidContainerItem fluidContainerItem = (IFluidContainerItem) stack.getItem();

                int maxFill = fluidContainerItem.fill(stack, tileEntityBarrel.drain(ForgeDirection.DOWN, MAX_TAP_AMOUNT, false), false);
                if (maxFill > 0)
                {
                    if (!world.isRemote)
                    {
                        FluidStack drained = tileEntityBarrel.drain(ForgeDirection.DOWN, maxFill, true);
                        fluidContainerItem.fill(stack, drained, true);
                    }
                }

                tileEntityBarrel.timeLeftTapOpen = 20;

                if (split && !player.inventory.addItemStackToInventory(stack))
                    world.spawnEntityInWorld(new EntityItem(world, player.posX, player.posY, player.posZ, stack));

                player.openContainer.detectAndSendChanges();

                return true;
            }
            else
            {
                if (!world.isRemote)
                    player.openGui(Psychedelicraft.instance, PSGuiHandler.barrelContainerID, world, x, y, z);

                return true;
            }
        }

        return false;
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister)
    {

    }

    @Override
    public String getItemIconName()
    {
        return getTextureName();
    }

    @Override
    public boolean hasTileEntity(int metadata)
    {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World var1, int var2)
    {
        return new TileEntityBarrel();
    }
}
