/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity;

import ivorius.psychedelicraft.blocks.PSBlocks;
import ivorius.psychedelicraft.config.PSConfig;
import ivorius.psychedelicraft.crafting.DryingRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.*;
import net.minecraft.world.Heightmap.Type;
import java.util.ArrayList;
import java.util.List;

public class DryingTableBlockEntity extends BlockEntity implements SidedInventory {

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(10, ItemStack.EMPTY);

    public int ticksAlive;

    public float heatRatio;
    public float dryingProgress;

    public ItemStack plannedResult = ItemStack.EMPTY;

    public DryingTableBlockEntity(BlockPos pos, BlockState state) {
        super(PSBlockEntities.DRYING_TABLE, pos, state);
    }

    public static void serverTick(ServerWorld world, BlockPos pos, BlockState state, DryingTableBlockEntity entity) {
        entity.tick(world);
    }

    public void tick(ServerWorld world) {

        ticksAlive++;
        float progress = dryingProgress;
        float oldHeat = heatRatio;

        if (ticksAlive % 30 == 5) {
            float l = world.getLightLevel(pos) / 15F;
            float h = !world.isAir(pos) ? world.getBiome(pos).value().getTemperature() * 0.75F + 0.25F : 0;
            heatRatio = MathHelper.clamp((l * l * h) * (l * l * h), 0, 1);

            if (world.getRainGradient(1) > 0 && world.getTopPosition(Type.MOTION_BLOCKING, pos).getY() == pos.getY() + 1) {
                dryingProgress = 0;
            }
        }

        if (!plannedResult.isEmpty()) {
            dryingProgress += heatRatio / (
                    world.getBlockState(pos).isOf(PSBlocks.dryingTableIron)
                    ? PSConfig.ironDryingTableTickDuration
                    : PSConfig.dryingTableTickDuration
            );

            if (dryingProgress >= 1) {
                endDryingProcess();
            }
        } else {
            dryingProgress = 0;
        }

        if (progress != dryingProgress || oldHeat != heatRatio) {
            world.getChunkManager().markForUpdate(pos);
        }
        markDirty();
    }

    public ItemStack getResult() {
        List<ItemStack> src = new ArrayList<>(size() - 1);
        for (int i = 1; i < size(); i++) {
            ItemStack stack = getStack(i);
            if (!stack.isEmpty()) {
                src.add(stack);
            }
        }

        ItemStack itemStack = DryingRegistry.dryingResult(src);
        return getStack(0).isEmpty() || ItemStack.areEqual(itemStack, getStack(0)) ? itemStack : ItemStack.EMPTY;
    }

    public void endDryingProcess() {
        dryingProgress = 0;

        for (int i = 1; i < size(); i++) {
            inventory.set(i, ItemStack.EMPTY);
        }

        if (getStack(0).isEmpty()) {
            setStack(0, plannedResult);
        } else {
            getStack(0).increment(plannedResult.getCount());
        }
        onInventoryChanged();
    }

    @Override
    public void writeNbt(NbtCompound compound) {
        super.writeNbt(compound);
        Inventories.writeNbt(compound, inventory);
        compound.put("plannedResult", plannedResult.writeNbt(new NbtCompound()));
        compound.putFloat("heatRatio", heatRatio);
        compound.putFloat("dryingProgress", dryingProgress);
    }

    @Override
    public void readNbt(NbtCompound compound) {
        super.readNbt(compound);
        Inventories.readNbt(compound, inventory);
        plannedResult = ItemStack.fromNbt(compound.getCompound("plannedResult"));
        heatRatio = compound.getFloat("heatRatio");
        dryingProgress = compound.getFloat("dryingProgress");
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
        plannedResult = getResult();
        dryingProgress = 0;

        markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return (getCachedState().equals(player.world.getBlockState(pos)))
                && player.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) <= 64;
    }

    @Override
    public int[] getAvailableSlots(Direction direction) {
        return direction == Direction.UP ? new int[]{ 0 } : new int[]{
                1, 2, 3, 4, 5, 6, 7, 8, 9
        };
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, Direction direction) {
        return isValid(slot, stack);
    }

    @Override
    public boolean canExtract(int var1, ItemStack stack, Direction direction) {
        return true;
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }
}
