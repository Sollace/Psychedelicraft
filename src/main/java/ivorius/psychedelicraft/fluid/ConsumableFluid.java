/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluid;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

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
        return stack.getItem() instanceof FluidContainer container
                && container.getLevel(stack) >= maxConsumed
                && container.getFluid(stack) instanceof ConsumableFluid consumable
                && consumable.canConsume(stack, entity, type);
    }

    static ItemStack consume(ItemStack stack, LivingEntity entity, int maxConsumed, boolean consume, ConsumptionType type) {
        if (stack.getItem() instanceof FluidContainer container) {
            if (container.getLevel(stack) >= maxConsumed) {
                if (container.getFluid(stack) instanceof ConsumableFluid consumable && consumable.canConsume(stack, entity, type)) {
                    MutableFluidContainer mutable = container.toMutable(stack);
                    MutableFluidContainer drained = mutable.drain(maxConsumed);
                    if (consume) {
                        consumable.consume(drained.asStack(), entity, type);
                        if (entity instanceof ServerPlayerEntity player) {
                            Criteria.CONSUME_ITEM.trigger(player, stack);
                        }
                    }
                    return mutable.asStack();
                }
            }
        }

        return stack;
    }

    public enum ConsumptionType {
        DRINK,
        INJECT
    }
}
