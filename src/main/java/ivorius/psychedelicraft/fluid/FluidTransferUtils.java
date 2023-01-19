package ivorius.psychedelicraft.fluid;

import java.util.function.IntConsumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.item.ItemStack;

public interface FluidTransferUtils {

    /**
     * Transfers fluid from one stack to another.
     * Returns a copy of the input stack with whatever remains after the transfer is completed.
     */
    static ItemStack transfer(int levels, ItemStack from, ItemStack to, @Nullable IntConsumer changeCallback) {
        return transfer(levels, FluidContainerItem.of(from), from, FluidContainerItem.of(to), to, changeCallback);
    }

    /**
     * Transfers fluid from one stack to another.
     * Returns a stack with whatever remains after the transfer is completed.
     */
    static ItemStack transfer(int levels,
            FluidContainerItem inputContainer, ItemStack input,
            FluidContainerItem outputContainer, ItemStack output, @Nullable IntConsumer changeCallback) {
        var inputFluid = inputContainer.getFluid(input);
        var outputFluid = outputContainer.getFluid(output);

        if (inputFluid.isEmpty() || (!outputFluid.isEmpty() && outputFluid != inputFluid)) {
            return input;
        }

        levels = Math.min(
            Math.min(levels, inputContainer.getFluidLevel(input)),
            outputContainer.getMaxCapacity(output) - outputContainer.getFluidLevel(output)
        );

        if (levels <= 0) {
            return input;
        }

        ItemStack remainder = input.copy();
        inputContainer.drain(remainder, levels);
        outputContainer.deposit(output, levels, inputFluid);
        if (changeCallback != null) {
            changeCallback.accept(levels);
        }
        return remainder;
    }
}
