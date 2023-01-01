/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.items;

import ivorius.psychedelicraft.blocks.TileEntityBarrel;
import ivorius.psychedelicraft.fluids.FluidFermentable;
import ivorius.psychedelicraft.fluids.FluidHelper;
import ivorius.psychedelicraft.fluids.FluidWithIconSymbol;
import ivorius.psychedelicraft.fluids.FluidWithIconSymbolRegistering;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class ItemBarrel extends ItemBlockFluidContainer
{
    public ItemBarrel(Block block)
    {
        super(block, TileEntityBarrel.BARREL_CAPACITY);
        setMaxStackSize(16);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override
    public int getItemStackLimit(ItemStack stack)
    {
        return getFluid(stack) == null ? maxStackSize : 1;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        FluidStack fluidStack = getFluid(stack);

        if (fluidStack != null)
        {
            String fluidName = fluidStack.getLocalizedName();
            return StatCollector.translateToLocalFormatted(this.getUnlocalizedNameInefficiently(stack) + ".full.name", fluidName);
        }

        return super.getItemStackDisplayName(stack);
    }

    @Override
    public boolean requiresMultipleRenderPasses()
    {
        return true;
    }

    @Override
    public int getRenderPasses(int metadata)
    {
        return 2;
    }

    @Override
    public IIcon getIcon(ItemStack stack, int pass)
    {
        if (pass == 1)
        {
            FluidStack fluidStack = getFluid(stack);
            if (fluidStack != null && fluidStack.getFluid() instanceof FluidWithIconSymbol)
            {
                IIcon iconSymbol = ((FluidWithIconSymbol) fluidStack.getFluid()).getIconSymbol(fluidStack, FluidWithIconSymbolRegistering.TEXTURE_TYPE_ITEM);
                if (iconSymbol != null)
                    return iconSymbol;
            }
        }

        return super.getIcon(stack, pass);
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
        return fluidStack != null ? 1.0 - ((double) fluidStack.amount / capacity) : 0.0;
    }
}
