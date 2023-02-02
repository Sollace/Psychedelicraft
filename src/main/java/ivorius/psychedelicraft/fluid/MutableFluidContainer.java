package ivorius.psychedelicraft.fluid;

import java.util.function.IntConsumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;

public final class MutableFluidContainer {
    public static MutableFluidContainer of(FluidContainer container, ItemStack stack) {
        return new MutableFluidContainer(container, container.getFluid(stack), container.getLevel(stack), FluidContainer.getFluidAttributesTag(stack, true));
    }

    private FluidContainer container;

    private SimpleFluid fluid;
    private int level;
    private NbtCompound attributes;

    private MutableFluidContainer(FluidContainer container, SimpleFluid fluid, int level, NbtCompound attributes) {
        this.container = container;
        this.fluid = fluid;
        this.level = level;
        this.attributes = attributes;
    }

    public MutableFluidContainer copy() {
        return new MutableFluidContainer(container, fluid, level, attributes.copy());
    }

    public ItemStack asStack() {
        if (isEmpty()) {
            return container.asEmpty().getDefaultStack();
        }

        ItemStack stack = container.asFilled(fluid).getDefaultStack();
        NbtCompound fluidTag = stack.getOrCreateSubNbt("fluid");
        fluidTag.putInt("level", level);
        fluidTag.putString("id", fluid.getId().toString());
        fluidTag.put("attributes", (isEmpty() ? FluidContainer.EMPTY_NBT : attributes).copy());
        return stack;
    }

    public boolean isEmpty() {
        return level <= 0 || fluid.isEmpty();
    }

    public int getLevel() {
        return level;
    }

    public SimpleFluid getFluid() {
        return fluid;
    }

    public int getCapacity() {
        return container.getMaxCapacity();
    }

    public MutableFluidContainer withLevel(int level) {
        this.level = MathHelper.clamp(level, 0, getCapacity());
        if (this.level == 0) {
            return withFluid(PSFluids.EMPTY);
        }
        return this;
    }

    public MutableFluidContainer withFluid(SimpleFluid fluid) {
        this.fluid = fluid;
        if (fluid.isEmpty()) {
            this.level = 0;
        }
        if (fluid.isEmpty()) {
            this.attributes = FluidContainer.EMPTY_NBT;
        }

        return this;
    }

    public MutableFluidContainer withAttributes(NbtCompound attributes) {
        this.attributes = isEmpty() ? FluidContainer.EMPTY_NBT : attributes.copy();
        return this;
    }

    /**
     * Removes a certain amount of fluid from the given stack and returns a
     * new stack containing the amount of fluid that was drained.
     */
    public MutableFluidContainer drain(int amount) {
        amount = Math.min(getLevel(), amount);
        MutableFluidContainer removed = copy().withLevel(amount);
        withLevel(getLevel() - amount);
        return removed;
    }

    /**
     * Adds fluid to this container.
     * Returns the remaining levels.
     */
    public int deposit(int amount, SimpleFluid fluid) {
        int newLevel = Math.min(getCapacity(), getLevel() + amount);
        if (getFluid().isEmpty()) {
            withFluid(fluid);
        } else if (getFluid() != fluid) {
            return amount;
        }
        withLevel(newLevel);
        return amount - newLevel;
    }

    /**
     * Copies the fluid information from another stack to this one.
     */
    public MutableFluidContainer fillFrom(MutableFluidContainer other) {
        if (other.isEmpty()) {
            return this;
        }

        attributes = other.attributes;
        return withFluid(getFluid()).withLevel(getLevel() + other.getLevel());
    }

    /**
     * Transfers fluid from this container to another.
     * Returns a stack with whatever remains after the transfer is completed.
     */
    public MutableFluidContainer transfer(int levels, MutableFluidContainer outputContainer, @Nullable IntConsumer changeCallback) {
        var inputFluid = getFluid();
        var outputFluid = outputContainer.getFluid();

        if (isEmpty()
                || (!outputFluid.isEmpty() && outputFluid != inputFluid)
                || !(outputContainer.attributes.isEmpty() || outputContainer.attributes.equals(attributes))) {
            return this;
        }

        levels = Math.min(
            Math.min(levels, getLevel()),
            outputContainer.getCapacity() - outputContainer.getLevel()
        );

        if (levels <= 0) {
            return this;
        }

        drain(levels);
        outputContainer.deposit(levels, inputFluid);
        outputContainer.attributes = attributes;
        if (changeCallback != null) {
            changeCallback.accept(levels);
        }
        return this;
    }
}
