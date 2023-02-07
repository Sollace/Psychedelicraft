package ivorius.psychedelicraft.fluid;

import java.util.function.IntConsumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.item.ItemStack;

/**
 * @author Sollace
 * @since 3 Jan 2023
 */
public interface FluidStore {

    MutableFluidContainer getContents();

    default boolean isEmpty() {
        return getContents().isEmpty();
    }

    default int getLevel() {
        return getContents().getLevel();
    }

    MutableFluidContainer deposit(int levels, MutableFluidContainer input, @Nullable IntConsumer changeCallback);

    MutableFluidContainer drain(int levels, MutableFluidContainer output, @Nullable IntConsumer changeCallback);

    default ItemStack getStack() {
        return getContents().asStack();
    }

    default ItemStack deposit(ItemStack stack) {
        return deposit(FluidContainer.of(stack).getMaxCapacity(stack), stack);
    }

    default ItemStack deposit(int levels, ItemStack input) {
        return deposit(levels, input, null);
    }

    default ItemStack deposit(int levels, ItemStack input, @Nullable IntConsumer changeCallback) {
        return deposit(levels, MutableFluidContainer.of(input), changeCallback).asStack();
    }

    default ItemStack drain(int levels, ItemStack output) {
        return drain(levels, output, null);
    }

    default ItemStack drain(int levels, ItemStack output, @Nullable IntConsumer changeCallback) {
        return drain(levels, MutableFluidContainer.of(output.copy()), changeCallback).asStack();
    }
}
