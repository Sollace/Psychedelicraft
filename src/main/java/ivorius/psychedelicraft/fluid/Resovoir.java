package ivorius.psychedelicraft.fluid;

import java.util.function.IntConsumer;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.util.NbtSerialisable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;

/**
 * @author Sollace
 * @since 3 Jan 2023
 */
public class Resovoir implements Inventory, NbtSerialisable {

    private FluidContainer container;
    private MutableFluidContainer stack;

    private final ChangeListener changeCallback;

    public Resovoir(int capacity, ChangeListener changeCallback) {
        this.container = FluidContainer.withCapacity(Items.STONE, capacity);
        this.stack = container.toMutable(Items.STONE.getDefaultStack());
        this.changeCallback = changeCallback;
    }

    public SimpleFluid getFluidType() {
        return stack.getFluid();
    }

    public int getLevel() {
        return stack.getLevel();
    }

    public ItemStack getStack() {
        return stack.asStack();
    }

    public MutableFluidContainer getContents() {
        return stack;
    }

    public int getCapacity() {
        return stack.getCapacity();
    }

    @Override
    public boolean isEmpty() {
        return getLevel() == 0 || getFluidType().isEmpty();
    }

    public ItemStack setCapacity(int capacity) {
        this.container = FluidContainer.withCapacity(container.asItem(), capacity);
        this.stack = container.toMutable(getStack());

        int level = getLevel();

        if (level > capacity) {
            int expunged = level - capacity;
            ItemStack clone = FluidContainer.UNLIMITED.toMutable(stack.asStack().copy()).withLevel(expunged).asStack();
            stack.withLevel(capacity);
            changeCallback.onDrain(this);
            return clone;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public int getMaxCountPerStack() {
        return 1;
    }

    public ItemStack deposit(ItemStack stack) {
        return deposit(FluidContainer.of(stack).getMaxCapacity(stack), stack);
    }

    public ItemStack deposit(int levels, ItemStack input) {
        return deposit(levels, input, null);
    }

    public ItemStack deposit(int levels, ItemStack input, @Nullable IntConsumer changeCallback) {
        return FluidContainer.of(input).toMutable(input).transfer(Math.min(getCapacity() - getLevel(), levels), stack, levelsChange -> {
            this.changeCallback.onFill(this, levelsChange);
            if (changeCallback != null) {
                changeCallback.accept(levelsChange);
            }
        }).asStack();
    }

    public ItemStack drain(int levels, ItemStack output) {
        return drain(levels, output, null);
    }

    public ItemStack drain(int levels, ItemStack output, @Nullable IntConsumer changeCallback) {
        MutableFluidContainer outputContainer = FluidContainer.of(output).toMutable(output.copy());
        stack.transfer(levels, outputContainer, levelsChange -> {
            this.changeCallback.onDrain(this);
            if (changeCallback != null) {
                changeCallback.accept(levelsChange);
            }
        });
        return outputContainer.asStack();
    }

    @Override
    public void clear() {
        stack = MutableFluidContainer.of(FluidContainer.UNLIMITED, Items.STONE.getDefaultStack());
        changeCallback.onDrain(this);
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public ItemStack getStack(int slot) {
        return stack.asStack();
    }

    @Override
    public ItemStack removeStack(int slot, int levels) {
        levels = Math.min(getLevel(), levels);
        ItemStack drained = stack.drain(levels).asStack();
        if (levels > 0) {
            this.changeCallback.onDrain(this);
        }
        return drained;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack s = stack.asStack();
        clear();
        return s;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        boolean wasEmpty = isEmpty();
        int oldLevel = getLevel();
        this.stack = MutableFluidContainer.of(FluidContainer.UNLIMITED, stack);
        if (isEmpty() != wasEmpty) {
            if (wasEmpty) {
                changeCallback.onFill(this, getLevel());
            } else {
                changeCallback.onDrain(this);
            }
        } else if (oldLevel != getLevel()) {
            if (oldLevel < getLevel()) {
                changeCallback.onFill(this, getLevel() - oldLevel);
            } else {
                changeCallback.onDrain(this);
            }
        } else {
            changeCallback.onIdle(this);
        }
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean canPlayerUse(PlayerEntity var1) {
        return true;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return !FluidContainer.of(stack).getFluid(stack).isEmpty();
    }

    @Override
    public void toNbt(NbtCompound compound) {
        compound.put("stack", stack.asStack().writeNbt(new NbtCompound()));
    }

    @Override
    public void fromNbt(NbtCompound compound) {
        stack = container.toMutable(ItemStack.fromNbt(compound.getCompound("stack")));
    }

    public interface ChangeListener {
        void onDrain(Resovoir resovoir);

        void onFill(Resovoir resovoir, int amountFilled);

        default void onIdle(Resovoir resovoir) {

        }
    }
}
