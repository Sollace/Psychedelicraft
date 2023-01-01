/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluids;

import net.minecraft.item.ItemStack;

/**
 * A fluid that can distill in the correct container, e.g. distillery.
 */
public interface FluidDistillable
{
    /**
     * Tick value indicating that the fluid is currently not distillable.
     */
    int UNDISTILLABLE = -1;

    /**
     * The creative subtype for FluidWithTypes.
     */
    String SUBTYPE = "distillable";

    /**
     * Returns the ticks needed for the fluid to distill. Return {@link #UNDISTILLABLE} if the fluid is curently not distillable.
     *
     * @param stack The fluid currently being distilled.
     * @return The time it needs to distill, in ticks.
     */
    int distillationTime(ItemStack stack);

    /**
     * Notifies the fluid that the stack has distilled, and is expected to apply this change to the stack.
     *
     * @param stack The fluid currently being distilled.
     * @return The stack left over in the distillery.
     */
    ItemStack distillStep(ItemStack stack);
}
