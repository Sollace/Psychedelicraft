package ivorius.psychedelicraft.util.compat;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public interface RecipeInput extends Inventory {
    ItemStack getStackInSlot(int slot);

    int getSize();

    @Override
    default boolean isEmpty() {
        for (int i = 0; i < getSize(); i++) {
            if (!getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    default void clear() { }

    @Override
    default int size() {
        return getSize();
    }

    @Override
    default ItemStack getStack(int slot) {
        return getStackInSlot(slot);
    }

    @Override
    default ItemStack removeStack(int slot, int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    default ItemStack removeStack(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    default void setStack(int slot, ItemStack stack) {
    }

    @Override
    default void markDirty() {

    }

    @Override
    default boolean canPlayerUse(PlayerEntity player) {
        return false;
    }
}
