/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluids;

import ivorius.psychedelicraft.items.FluidContainerItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;

/**
 * A fluid that is possible to be consumed.
 */
public interface ConsumableFluid {
    /**
     * Indicates if the entity can inject this fluid.
     *
     * @param fluidStack The fluid stack.
     * @param entity     The entity about to inject the fluid.
     * @return True if the entity can inject this fluid, at this point in time.
     */
    boolean canConsume(ItemStack stack, LivingEntity entity, ConsumptionType type);
    /**
     * Called when the entity has injected the fluid.
     *
     * @param fluidStack The fluid stack.
     * @param entity     The entity injecting the fluid.
     */
    void consume(ItemStack stack, LivingEntity entity, ConsumptionType type);

    static boolean canConsume(ItemStack stack, LivingEntity entity, int maxConsumed, ConsumptionType type) {
        return stack.getItem() instanceof FluidContainerItem container
                && container.getFluidLevel(stack) >= maxConsumed
                && container.getFluid(stack) instanceof ConsumableFluid consumable
                && consumable.canConsume(stack, entity, type);
    }

    static ItemStack consume(ItemStack stack, LivingEntity entity, int maxConsumed, boolean consume, ConsumptionType type) {
        if (stack.getItem() instanceof FluidContainerItem container) {
            if (container.getFluidLevel(stack) >= maxConsumed) {
                Fluid fluid = container.getFluid(stack);
                if (fluid instanceof ConsumableFluid consumable && consumable.canConsume(stack, entity, type)) {
                    ItemStack drained = container.drain(stack, maxConsumed);
                    if (consume) {
                        consumable.consume(drained, entity, type);
                    }
                    return drained;
                }
            }
        }

        return ItemStack.EMPTY;
    }

    public enum ConsumptionType {
        DRINK,
        INJECT
    }
}
