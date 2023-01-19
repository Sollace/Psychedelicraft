/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluid;

import net.minecraft.item.ItemStack;

/**
 * A fluid that can explode.
 */
public interface Combustable {
    Combustable NON_COMBUSTABLE = new Combustable() {
        @Override
        public float getFireStrength(ItemStack stack) {
            return 0;
        }

        @Override
        public float getExplosionStrength(ItemStack stack) {
            return 0;
        }
    };

    /**
     * Determines the flame distance of the explosion.
     *
     * @param fluidStack The fluid stack.
     * @return The flame distance in blocks.
     */
    float getFireStrength(ItemStack stack);

    /**
     * Determines the explosion size.
     *
     * @param fluidStack The fluid stack.
     * @return The explosion size in blocks.
     */
    float getExplosionStrength(ItemStack stack);

    static Combustable fromStack(ItemStack stack) {
        if (FluidContainerItem.of(stack).getFluid(stack) instanceof Combustable exploder) {
            return exploder;
        }

        return NON_COMBUSTABLE;
    }
}
