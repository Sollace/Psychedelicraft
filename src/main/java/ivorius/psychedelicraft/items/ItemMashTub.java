/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.items;

import ivorius.ivtoolkit.blocks.IvMultiBlockHelper;
import ivorius.ivtoolkit.blocks.IvTileEntityMultiBlock;
import ivorius.psychedelicraft.blocks.TileEntityMashTub;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

import java.util.List;

public class ItemMashTub extends ItemBlockFluidContainer
{
    public ItemMashTub(Block block)
    {
        super(block, TileEntityMashTub.MASH_TUB_CAPACITY);
        setMaxStackSize(1);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override
    public boolean onItemUse(ItemStack itemStack, EntityPlayer entityPlayer, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
        int width = 2;
        int height = 1;

        int rotation = 0;
        List<int[]> positions = IvMultiBlockHelper.getRotatedPositions(rotation, width, height, width);

        IvMultiBlockHelper multiBlockHelper = new IvMultiBlockHelper();
        if (multiBlockHelper.beginPlacing(positions, world, x, y, z, side, itemStack, entityPlayer, this.field_150939_a, 0, rotation))
        {
            for (int[] position : multiBlockHelper)
            {
                IvTileEntityMultiBlock tileEntity = multiBlockHelper.placeBlock(position);

                if (tileEntity instanceof TileEntityMashTub)
                {
                    TileEntityMashTub mashTub = (TileEntityMashTub) tileEntity;
                    if (tileEntity.isParent())
                    {
                        FluidStack fluidStack = itemStack.getItem() instanceof IFluidContainerItem ? ((IFluidContainerItem) itemStack.getItem()).getFluid(itemStack) : null;
                        if (fluidStack != null)
                            mashTub.fill(ForgeDirection.UP, fluidStack, true);
                    }
                }
            }

            itemStack.stackSize--;
        }

        return true;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        FluidStack fluidStack = getFluid(stack);

        if (fluidStack != null)
        {
            String fluidName = fluidStack.getLocalizedName();
            return I18n.format(this.getUnlocalizedNameInefficiently(stack) + ".full.name", fluidName);
        }

        return super.getItemStackDisplayName(stack);
    }

    @Override
    public void registerIcons(IIconRegister iconRegister)
    {
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack)
    {
        FluidStack fluidStack = getFluid(stack);
        return fluidStack != null && fluidStack.amount < capacity;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack)
    {
        FluidStack fluidStack = getFluid(stack);
        return fluidStack != null ? 1.0 - ((double) fluidStack.amount / (double) capacity) : 0.0;
    }
}
