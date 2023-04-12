package ivorius.psychedelicraft.fluid;

import java.util.function.IntConsumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.MathHelper;

public class MutableFluidContainer {

    public static MutableFluidContainer of(ItemStack stack) {
        return FluidContainer.of(stack).toMutable(stack);
    }

    protected FluidContainer container;

    protected SimpleFluid fluid;
    protected int level;
    protected NbtCompound attributes;

    @Nullable
    protected NbtCompound stackNbt;

    protected MutableFluidContainer(FluidContainer container, SimpleFluid fluid, int level, NbtCompound attributes, @Nullable NbtCompound stackNbt) {
        this.container = container;
        this.fluid = fluid;
        this.level = level;
        this.attributes = attributes;
        this.stackNbt = stackNbt;
    }

    public MutableFluidContainer copy() {
        return new MutableFluidContainer(container, getFluid(), getLevel(), attributes.copy(), stackNbt == null ? null : stackNbt.copy());
    }

    public ItemStack asStack() {
        if (isEmpty()) {
            return container.asEmpty().getDefaultStack();
        }

        ItemStack stack = container.asFilled(getFluid()).getDefaultStack();
        stack.setNbt(stackNbt == null ? null : stackNbt.copy());
        NbtCompound fluidTag = stack.getOrCreateSubNbt("fluid");
        fluidTag.putInt("level", getLevel());
        fluidTag.putString("id", getFluid().getId().toString());
        fluidTag.put("attributes", (isEmpty() ? FluidContainer.EMPTY_NBT : attributes).copy());
        return stack;
    }

    public boolean isEmpty() {
        return getLevel() <= 0 || getFluid().isEmpty();
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

    public NbtCompound getAttributes() {
        return attributes.copy();
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
        return withFluid(other.getFluid()).withLevel(getLevel() + other.getLevel());
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
                || !(outputFluid.isEmpty() || outputContainer.attributes.isEmpty() || NbtHelper.matches(attributes, outputContainer.attributes, true))) {
            return this;
        }

        levels = Math.min(
            Math.min(levels, getLevel()),
            outputContainer.getCapacity() - outputContainer.getLevel()
        );

        if (levels <= 0) {
            return this;
        }

        outputContainer.attributes = attributes.copy();
        drain(levels);
        outputContainer.deposit(levels, inputFluid);
        if (changeCallback != null) {
            changeCallback.accept(levels);
        }
        return this;
    }

    public boolean canReceive(SimpleFluid fluid) {
        return !fluid.isEmpty()
            && (isEmpty() || getFluid() == fluid)
            && getLevel() < getCapacity()
            && fluid.isSuitableContainer(container);
    }
}
