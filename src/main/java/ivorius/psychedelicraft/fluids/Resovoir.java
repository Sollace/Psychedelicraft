package ivorius.psychedelicraft.fluids;

import ivorius.psychedelicraft.items.FluidContainerItem;
import ivorius.psychedelicraft.util.NbtSerialisable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

/**
 * @author Sollace
 * @since 3 Jan 2023
 */
public class Resovoir implements Inventory, NbtSerialisable {
    private int capacity;

    private ItemStack stack;

    private final ChangeListener changeCallback;

    public Resovoir(int capacity, ChangeListener changeCallback) {
        this.capacity = capacity;
        this.changeCallback = changeCallback;
    }

    public SimpleFluid getFluidType() {
        return FluidContainerItem.FLUID.getFluid(stack);
    }

    public int getLevel() {
        return FluidContainerItem.FLUID.getFluidLevel(stack);
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
            FluidContainerItem.FLUID.setLevel(clone, expunged);
            stack = FluidContainerItem.FLUID.setLevel(stack, capacity);
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

    public ItemStack deposit(int levels, ItemStack stack) {
        var incoming = FluidContainerItem.of(stack);
        SimpleFluid fluid = incoming.getFluid(stack);

        if (fluid.isEmpty() || (!isEmpty() && getFluidType() != fluid)) {
            return stack;
        }

        int available = Math.min(levels, incoming.getFluidLevel(stack));
        int newLevel = Math.min(getCapacity(), getLevel() + available);
        if (newLevel > getLevel()) {
            changeCallback.onFill(this, getLevel() - newLevel);
        }
        ItemStack copy = stack.copy();
        incoming.setLevel(copy, Math.max(0, available - getCapacity()));
        FluidContainerItem.FLUID.setLevel(this.stack, newLevel);

        return copy;
    }

    public ItemStack drain(int levels) {
        ItemStack stack = this.stack.copy();
        int withdrawn = Math.min(levels, getLevel());
        FluidContainerItem.FLUID.setLevel(stack, withdrawn);
        FluidContainerItem.FLUID.setLevel(this.stack, getLevel() - withdrawn);
        changeCallback.onDrain(this);
        return stack;
    }

    public ItemStack drain(int levels, ItemStack container) {
        ItemStack stack = container.copy();
        int withdrawn = Math.min(FluidContainerItem.of(stack).getMaxCapacity(stack), Math.min(levels, getLevel()));
        FluidContainerItem.of(stack).setLevel(stack, withdrawn);
        FluidContainerItem.FLUID.setLevel(this.stack, getLevel() - withdrawn);
        changeCallback.onDrain(this);
        return stack;
    }

    @Override
    public void clear() {
        stack = ItemStack.EMPTY;
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
    public ItemStack removeStack(int slot, int count) {
        return drain(count);
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
