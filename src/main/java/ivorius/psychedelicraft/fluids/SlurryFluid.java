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
public class SlurryFluid extends SimpleFluid implements Processable {
    public static final int FLUID_PER_DIRT = FluidHelper.MILLIBUCKETS_PER_LITER * 4;

    public SlurryFluid(Identifier id, Settings settings) {
        super(id, settings);
    }

    @Override
    public int getProcessingTime(ItemStack stack, ProcessType type, boolean openContainer) {
        if (type == ProcessType.FERMENT) {
            return stack.getCount() >= FLUID_PER_DIRT ? PSConfig.slurryHardeningTime : UNCONVERTABLE;
        }
        return UNCONVERTABLE;
    }

    @Override
    public ItemStack process(ItemStack stack, ProcessType type, boolean openContainer) {
        if (type == ProcessType.FERMENT) {
            return new ItemStack(Items.DIRT, stack.getCount() / FLUID_PER_DIRT);
        }
        return ItemStack.EMPTY;
    }
}
