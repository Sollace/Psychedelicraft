/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity;

import java.util.stream.Stream;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.*;

public abstract class BlockEntityWithInventory extends LockableContainerBlockEntity implements SidedInventory {
    protected static final int[] NO_SLOTS = new int[0];

    private final DefaultedList<ItemStack> inventory;

    public BlockEntityWithInventory(BlockEntityType<? extends BlockEntityWithInventory> type, BlockPos pos, BlockState state, int size) {
        super(type, pos, state);
        inventory = DefaultedList.ofSize(size, ItemStack.EMPTY);
    }

    protected Stream<ItemStack> getStacks() {
        return inventory.stream();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return false;
    }

    @Override
    public ItemStack getStack(int slot) {
        return inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack removed = Inventories.splitStack(inventory, slot, amount);
        if (!removed.isEmpty()) {
            markDirty();
        }
        return removed;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        inventory.set(slot, stack);
        markDirty();
    }

    @Override
    public int size() {
        return inventory.size();
    }

    @Override
    public void clear() {
        markDirty();
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack removed = Inventories.removeStack(inventory, slot);
        if (!removed.isEmpty()) {
            markDirty();
        }
        return removed;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (world instanceof ServerWorld sw) {
            sw.getChunkManager().markForUpdate(getPos());
        }
        if (world != null) {
            if (getCachedState().hasComparatorOutput()) {
                world.updateComparators(pos, getCachedState().getBlock());
            }
            world.updateNeighbors(pos, getCachedState().getBlock());
        }
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return Inventory.canPlayerUse(this, player);
    }

    @Override
    protected Text getContainerName() {
        return getCachedState().getBlock().getName();
    }


    @Override
    public boolean canInsert(int slot, ItemStack stack, Direction direction) {
        return isValid(slot, stack);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction direction) {
        return true;
    }

    @Override
    public final Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public final NbtCompound toInitialChunkDataNbt() {
        NbtCompound compound = super.toInitialChunkDataNbt();
        writeNbt(compound);
        return compound;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        inventory.clear();
        Inventories.readNbt(nbt, inventory);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);
    }
}
