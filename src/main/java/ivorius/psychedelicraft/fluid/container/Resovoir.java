package ivorius.psychedelicraft.fluid.container;

import java.util.function.IntConsumer;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.util.NbtSerialisable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;

/**
 * @author Sollace
 * @since 3 Jan 2023
 */
public class Resovoir implements NbtSerialisable, VariantMarshal.StorageMarshal, FluidStore {
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

    public void transferTo(Resovoir tank) {
        tank.stack = stack;
        stack = container.toMutable(Items.STONE.getDefaultStack());
    }

    @Override
    public MutableFluidContainer getContents() {
        return stack;
    }

    @Override
    public int getMaxCountPerStack() {
        return 1;
    }

    @Override
    public MutableFluidContainer deposit(int levels, MutableFluidContainer input, @Nullable IntConsumer changeCallback) {
        return input.transfer((int)Math.min(getCapacity() - getLevel(), levels), stack, levelsChange -> {
            this.changeCallback.onFill(this, levelsChange);
            if (changeCallback != null) {
                changeCallback.accept(levelsChange);
            }
        });
    }

    @Override
    public MutableFluidContainer drain(int levels, MutableFluidContainer output, @Nullable IntConsumer changeCallback) {
        stack.transfer(levels, output, levelsChange -> {
            this.changeCallback.onDrain(this);
            if (changeCallback != null) {
                changeCallback.accept(levelsChange);
            }
        });
        return output;
    }

    @Override
    public void clear() {
        stack = container.toMutable(Items.STONE.getDefaultStack());
        changeCallback.onDrain(this);
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public ItemStack getStack(int slot) {
        return getStack();
    }

    @Override
    public ItemStack removeStack(int slot, int count) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        boolean wasEmpty = isEmpty();
        int oldLevel = getLevel();
        this.stack = container.toMutable(stack);
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
