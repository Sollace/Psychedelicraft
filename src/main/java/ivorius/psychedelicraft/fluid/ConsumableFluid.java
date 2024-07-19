/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluid;

import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.UseAction;

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
    void consume(ItemFluids stack, LivingEntity entity, ConsumptionType type);

    static boolean canConsume(ItemStack stack, LivingEntity entity, int maxConsumed, ConsumptionType type) {
        ItemFluids fluids = ItemFluids.of(stack);
        return fluids.amount() > 0
                && fluids.fluid() instanceof ConsumableFluid consumable
                && consumable.canConsume(stack, entity, type);
    }

    static ItemStack consume(ItemStack stack, LivingEntity entity, int maxConsumed, boolean consumeItem, ConsumptionType type) {
        if (canConsume(stack, entity, maxConsumed, type)) {
            if (ItemFluids.of(stack).fluid() instanceof ConsumableFluid consumable) {
                ItemFluids.Transaction transaction = ItemFluids.Transaction.begin(stack);
                ItemFluids withdrawn = transaction.withdraw(maxConsumed);
                if (!withdrawn.isEmpty()) {
                    consumable.consume(withdrawn, entity, type);
                    if (entity instanceof ServerPlayerEntity player) {
                        Criteria.CONSUME_ITEM.trigger(player, stack);
                    }
                }
                return consumeItem ? transaction.toItemStack() : stack;
            }
        }

        return stack;
    }

    public enum ConsumptionType {
        DRINK(UseAction.DRINK),
        INJECT(UseAction.BOW);

        private final UseAction action;

        ConsumptionType(UseAction action) {
            this.action = action;
        }

        public UseAction getUseAction() {
            return action;
        }
    }
}
