/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.items;

import ivorius.psychedelicraft.client.rendering.RenderPassesCustom;
import ivorius.psychedelicraft.fluids.FluidHelper;
import ivorius.psychedelicraft.fluids.InjectableFluid;
import net.minecraft.item.*;
import net.minecraft.world.World;

import java.util.List;

public class InjectableItem extends BucketItem implements RenderPassesCustom
{
    public static final int FLUID_PER_INJECTION = 10;

    public IIcon iconLiquid;

    public InjectableItem(Settings settings, int capacity)
    {
        super(settings, 0, capacity);

        this.setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override
    public int getItemStackLimit(ItemStack stack)
    {
        return getFluid(stack) == null ? maxStackSize : 1;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack par1ItemStack)
    {
        return EnumAction.bow;
    }

    @Override
    public ItemStack onEaten(ItemStack stack, World world, EntityPlayer player)
    {
        FluidHelper.inject(stack, player, FLUID_PER_INJECTION, true);

        return super.onEaten(stack, world, player);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
    {
        if (FluidHelper.inject(par1ItemStack, par3EntityPlayer, FLUID_PER_INJECTION, false) != null)
        {
            par3EntityPlayer.setItemInUse(par1ItemStack, getMaxItemUseDuration(par1ItemStack));
        }

        return par1ItemStack;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack par1ItemStack)
    {
        return 25;
    }

    @Override
    public boolean shouldRotateAroundWhenRendering()
    {
        return false;
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

        return StatCollector.translateToLocalFormatted(this.getUnlocalizedNameInefficiently(stack) + ".name");
    }

    @Override
    public void getSubItems(Item item, CreativeTabs tab, List list)
    {
        super.getSubItems(item, tab, list);

        for (FluidStack fluidStack : FluidHelper.allFluids(InjectableFluid.SUBTYPE, capacity))
        {
            ItemStack stack = new ItemStack(item);
            fill(stack, fluidStack, true);
            list.add(stack);
        }
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase entityLivingBase, EntityLivingBase par3EntityLivingBase)
    {
        if (!entityLivingBase.worldObj.isRemote && itemRand.nextFloat() < 0.5f)
            FluidHelper.inject(stack, entityLivingBase, FLUID_PER_INJECTION, true);

        return true;
    }

    @Override
    public IIcon getIcon(ItemStack stack, int pass)
    {
        if (getFluid(stack) == null)
            pass++;

        if (pass == 0)
            return iconLiquid;
        else
            return super.getIcon(stack, pass);
    }

    @Override
    public void registerIcons(IIconRegister iconRegister)
    {
        super.registerIcons(iconRegister);
        iconLiquid = iconRegister.registerIcon(getIconString() + "_liquid");
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
        return getFluid(stack) != null ? 2 : 1;
    }

    @Override
    public int getColorFromItemStack(ItemStack stack, int pass)
    {
        if (getFluid(stack) == null)
            pass++;

        if (pass == 0)
            return FluidHelper.getTranslucentFluidColor(stack);
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
}
