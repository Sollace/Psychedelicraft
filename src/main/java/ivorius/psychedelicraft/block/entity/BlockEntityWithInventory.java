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
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
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
    protected DefaultedList<ItemStack> getHeldStacks() {
        return inventory;
    }

    @Override
    protected void setHeldStacks(DefaultedList<ItemStack> inventory) {
        for (int i = 0; i < size(); i++) {
            this.inventory.set(i, i >= inventory.size() ? ItemStack.EMPTY : inventory.get(i));
        }
        markDirty();
    }

    @Override
    public int size() {
        return inventory.size();
    }

    @Override
    public void clear() {
        super.clear();
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
    public final NbtCompound toInitialChunkDataNbt(WrapperLookup lookup) {
        NbtCompound compound = super.toInitialChunkDataNbt(lookup);
        writeNbt(compound, lookup);
        return compound;
    }

    @Override
    protected void readNbt(NbtCompound nbt, WrapperLookup lookup) {
        super.readNbt(nbt, lookup);
        inventory.clear();
        Inventories.readNbt(nbt, inventory, lookup);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, WrapperLookup lookup) {
        super.writeNbt(nbt, lookup);
        Inventories.writeNbt(nbt, inventory, lookup);
    }
}
