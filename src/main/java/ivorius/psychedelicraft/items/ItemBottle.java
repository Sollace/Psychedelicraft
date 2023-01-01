/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.items;

import ivorius.psychedelicraft.client.rendering.RenderPassesCustom;
import ivorius.psychedelicraft.fluids.FluidHelper;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.*;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.ItemFluidContainer;

/**
 * Created by lukas on 21.10.14.
 */
public class ItemBottle extends BucketItem implements RenderPassesCustom
{
    private IIcon liquidIcon;
    private IIcon overlayIcon;

    public ItemBottle(int capacity)
    {
        super(0, capacity);
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
    public void registerIcons(IIconRegister par1IconRegister)
    {
        super.registerIcons(par1IconRegister);
        liquidIcon = par1IconRegister.registerIcon(getIconString() + "_liquid");
        overlayIcon = par1IconRegister.registerIcon(getIconString() + "_overlay");
    }

    @Override
    public IIcon getIcon(ItemStack stack, int pass)
    {
        if (getFluid(stack) == null)
            pass++;

        return pass == 0 ? liquidIcon : pass == 1 ? super.getIcon(stack, pass) : overlayIcon;
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

    // Required because otherwise, stack is not passed for icon query
    @Override
    public boolean requiresMultipleRenderPasses()
    {
        return true;
    }

    @Override
    public int getRenderPasses(int metadata)
    {
        return 1;
    }

    @Override
    public boolean hasAlphaCustom(ItemStack stack, int pass)
    {
        return true;
    }

    @Override
    public int getRenderPassesCustom(ItemStack stack)
    {
        return getFluid(stack) != null ? 3 : 2;
    }

    @Override
    public int getColorFromItemStack(ItemStack stack, int pass)
    {
        if (getFluid(stack) == null)
            pass++;

        if (pass == 0)
            return FluidHelper.getTranslucentFluidColor(stack);
        else if (pass == 1)
            return ItemDye.field_150922_c[stack.getItemDamage() % ItemDye.field_150922_c.length] | 0xff000000;
        else
            return super.getColorFromItemStack(stack, pass) | 0xff000000;
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

    public float[] getBottleColor(ItemStack stack)
    {
        return EntitySheep.fleeceColorTable[EntitySheep.fleeceColorTable.length - 1 - (stack.getItemDamage() % EntitySheep.fleeceColorTable.length)];
    }
}
