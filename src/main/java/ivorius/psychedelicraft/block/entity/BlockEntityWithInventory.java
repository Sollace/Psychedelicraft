/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.*;

public abstract class BlockEntityWithInventory extends SyncedBlockEntity implements SidedInventory {
    protected static final int[] NO_SLOTS = new int[0];

    private final DefaultedList<ItemStack> inventory;

    public BlockEntityWithInventory(BlockEntityType<? extends BlockEntityWithInventory> type, BlockPos pos, BlockState state, int size) {
        super(type, pos, state);
        inventory = DefaultedList.ofSize(size, ItemStack.EMPTY);
    }

    @Override
    public void writeNbt(NbtCompound compound) {
        super.writeNbt(compound);
        Inventories.writeNbt(compound, inventory);
    }

    @Override
    public void readNbt(NbtCompound compound) {
        super.readNbt(compound);
        Inventories.readNbt(compound, inventory);
    }

    @Override
    public int size() {
        return inventory.size();
    }

    @Override
    public ItemStack getStack(int slot) {
        return inventory.get(slot);
    }

    @Override
    public void clear() {
        inventory.clear();
    }

    @Override
    public boolean isEmpty() {
        return inventory.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(inventory, slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return Inventories.splitStack(inventory, slot, amount);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        inventory.set(slot, stack);
        onInventoryChanged();
    }

    @Override
    public int getMaxCountPerStack() {
        return 1;
    }

    public void onInventoryChanged() {
        markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return (getCachedState().equals(player.getWorld().getBlockState(pos)))
                && player.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) <= 64;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, Direction direction) {
        return isValid(slot, stack);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction direction) {
        return true;
    }
}
