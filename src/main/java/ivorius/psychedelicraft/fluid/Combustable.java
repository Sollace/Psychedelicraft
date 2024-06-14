/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluid;

import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.item.ItemStack;

/**
 * A fluid that can explode.
 */
public interface Combustable {
    Combustable NON_COMBUSTABLE = new Combustable() {
        @Override
        public float getFireStrength(ItemFluids stack) {
            return 0;
        }

        @Override
        public float getExplosionStrength(ItemFluids stack) {
            return 0;
        }
    };

    /**
     * Determines the flame distance of the explosion.
     *
     * @param fluidStack The fluid stack.
     * @return The flame distance in blocks.
     */
    float getFireStrength(ItemFluids stack);

    /**
     * Determines the explosion size.
     *
     * @param fluidStack The fluid stack.
     * @return The explosion size in blocks.
     */
    float getExplosionStrength(ItemFluids stack);

    static Combustable fromStack(ItemStack stack) {
        if (ItemFluids.of(stack).fluid() instanceof Combustable exploder) {
            return exploder;
        }

        return NON_COMBUSTABLE;
    }

    static Combustable fromStack(ItemFluids stack) {
        return stack.fluid() instanceof Combustable exploder ? exploder : NON_COMBUSTABLE;
    }
}
