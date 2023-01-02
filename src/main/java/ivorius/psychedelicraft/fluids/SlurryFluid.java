/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluids;

import ivorius.psychedelicraft.config.PSConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

/**
 * Created by lukas on 27.10.14.
 */
public class SlurryFluid extends SimpleFluid implements Fermentable {
    public static final int FLUID_PER_DIRT = FluidHelper.MILLIBUCKETS_PER_LITER * 4;

    public SlurryFluid(Identifier id, Settings settings) {
        super(id, settings);
    }

    @Override
    public int getFermentationTime(ItemStack stack, boolean openContainer) {
        return stack.getCount() >= FLUID_PER_DIRT ? PSConfig.slurryHardeningTime : UNFERMENTABLE;
    }

    @Override
    public ItemStack ferment(ItemStack stack, boolean openContainer) {
        return new ItemStack(Items.DIRT, stack.getCount() / FLUID_PER_DIRT);
    }
}
