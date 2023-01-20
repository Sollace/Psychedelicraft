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
    private int capacity;

    private ItemStack stack = Items.STONE.getDefaultStack();

    private final ChangeListener changeCallback;

    public Resovoir(int capacity, ChangeListener changeCallback) {
        this.capacity = capacity;
        this.changeCallback = changeCallback;
    }

    public SimpleFluid getFluidType() {
        return FluidContainerItem.UNLIMITED.getFluid(stack);
    }

    public int getLevel() {
        return FluidContainerItem.UNLIMITED.getFluidLevel(stack);
    }

    public ItemStack getStack() {
        return stack;
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    public boolean isEmpty() {
        return getLevel() == 0 || getFluidType().isEmpty();
    }

    public ItemStack setCapacity(int capacity) {
        this.capacity = capacity;

        int level = getLevel();

        if (level > capacity) {
            int expunged = level - capacity;
            ItemStack clone = stack.copy();
            FluidContainerItem.UNLIMITED.setLevel(clone, expunged);
            stack = FluidContainerItem.UNLIMITED.setLevel(stack, capacity);
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
        return deposit(FluidContainerItem.of(stack).getMaxCapacity(stack), stack);
    }

    public ItemStack deposit(int levels, ItemStack input) {
        return deposit(levels, input, null);
    }

    public ItemStack deposit(int levels, ItemStack input, @Nullable IntConsumer changeCallback) {
        return FluidTransferUtils.transfer(Math.min(getCapacity() - getLevel(), levels),
                FluidContainerItem.of(input), input,
                FluidContainerItem.UNLIMITED, stack, levelsChange -> {
            this.changeCallback.onFill(this, levelsChange);
            if (changeCallback != null) {
                changeCallback.accept(levelsChange);
            }
        });
    }

    public ItemStack drain(int levels, ItemStack output) {
        return drain(levels, output, null);
    }

    public ItemStack drain(int levels, ItemStack output, @Nullable IntConsumer changeCallback) {
        output = output.copy();

        ItemStack stack = this.stack;
        this.stack = FluidTransferUtils.transfer(levels,
                FluidContainerItem.UNLIMITED, this.stack,
                FluidContainerItem.of(output), output, levelsChange -> {
                    if (levelsChange < 0) {
                        this.changeCallback.onDrain(this);
                    }
                    if (changeCallback != null) {
                        changeCallback.accept(levelsChange);
                    }
        });
        if (stack != this.stack) {
            this.changeCallback.onDrain(this);
        }
        return output;
    }

    @Override
    public void clear() {
        stack = Items.STONE.getDefaultStack();
        changeCallback.onDrain(this);
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public ItemStack getStack(int slot) {
        return stack;
    }

    @Override
    public ItemStack removeStack(int slot, int levels) {
        levels = Math.min(getLevel(), levels);
        ItemStack drained = FluidContainerItem.UNLIMITED.drain(stack, levels);
        if (levels > 0) {
            this.changeCallback.onDrain(this);
        }
        return drained;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack s = stack;
        clear();
        return s;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        boolean wasEmpty = isEmpty();
        int oldLevel = getLevel();
        this.stack = stack;
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
        return !FluidContainerItem.of(stack).getFluid(stack).isEmpty();
    }

    @Override
    public void toNbt(NbtCompound compound) {
        compound.put("stack", stack.writeNbt(new NbtCompound()));
    }

    @Override
    public void fromNbt(NbtCompound compound) {
        stack = ItemStack.fromNbt(compound.getCompound("stack"));
    }

    public interface ChangeListener {
        void onDrain(Resovoir resovoir);

        void onFill(Resovoir resovoir, int amountFilled);

        default void onIdle(Resovoir resovoir) {

        }
    }
}
