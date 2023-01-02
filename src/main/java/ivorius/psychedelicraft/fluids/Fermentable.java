/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluids;

import net.minecraft.item.ItemStack;

/**
 * A fluid that can ferment in the correct container, e.g. fermentation barrels.
 */
public interface Fermentable {
    /**
     * Tick value indicating that the fluid is currently unfermentable.
     */
    int UNFERMENTABLE = -1;

    /**
     * The creative subtype for FluidWithTypes.
     */
    String SUBTYPE_OPEN = "openFermentation";

    /**
     * The creative subtype for FluidWithTypes.
     */
    String SUBTYPE_CLOSED = "closedContainer";

    /**
     * Returns the ticks needed for the fluid to ferment. Return {@link #UNFERMENTABLE} if the fluid is curently unfermentable.
     *
     * @param stack         The fluid currently fermenting.
     * @param openContainer True if the fluid is exposed to oxygen.
     * @return The time it needs to ferment, in ticks.
     */
    int getFermentationTime(ItemStack stack, boolean openContainer);

    /**
     * Notifies the fluid that the stack has fermented, and is expected to apply this change to the stack.
     *
     * @param stack         The fluid currently fermenting.
     * @param openContainer True if the fluid is exposed to oxygen.
     * @return A stack if the fluid should be replaced with a solid.
     */
    ItemStack ferment(ItemStack stack, boolean openContainer);
}
