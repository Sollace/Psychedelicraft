/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Pair;

import ivorius.psychedelicraft.block.BlockWithFluid;
import ivorius.psychedelicraft.block.BlockWithFluid.DirectionalFluidResovoir;
import ivorius.psychedelicraft.block.BurnerBlock;
import ivorius.psychedelicraft.block.PipeInsertable;
import ivorius.psychedelicraft.block.entity.contents.EmptyContents;
import ivorius.psychedelicraft.block.entity.contents.LargeContents;
import ivorius.psychedelicraft.block.entity.contents.SmallContents;
import ivorius.psychedelicraft.fluid.Processable;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.util.NbtSerialisable;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

public class BurnerBlockEntity extends SyncedBlockEntity implements BlockWithFluid.DirectionalFluidResovoir {
    static final int[] CONTAINER_SLOT_ID = {0};

    private int temperature;
    private ItemStack container = ItemStack.EMPTY;

    private Contents contents = new EmptyContents(this);

    public BurnerBlockEntity(BlockPos pos, BlockState state) {
        super(PSBlockEntities.BUNSEN_BURNER, pos, state);
    }

    public void setContainer(ItemStack container) {
        this.container = container;
        markDirty();
    }

    public ItemStack getContainer() {
        return container;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
        markDirty();
    }

    public Contents getContents() {
        return contents;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (getWorld() instanceof ServerWorld sw) {
            sw.getChunkManager().markForUpdate(getPos());
        }
    }

    public boolean interact(ItemStack stack, PlayerEntity player, Hand hand, Direction side) {

        if (hand != Hand.MAIN_HAND) {
            return false;
        }

        var action = contents.interact(stack, player, hand, side);
        contents = action.getValue();
        return action.getResult().isAccepted();
    }

    public void playSound(@Nullable PlayerEntity player, SoundEvent sound) {
        if (world != null && pos != null) {
            world.playSound(player, pos.up(), sound, SoundCategory.BLOCKS, 1, world.random.nextFloat() * 0.4F + 0.8F);
        }
    }

    @Override
    public void tick(ServerWorld world) {
        if (world.getTime() % 14 == 0) {
            if (getCachedState().get(BurnerBlock.LIT)) {
                if (temperature < (getPrimaryTank().getContents().isEmpty() ? 200 : 100)) {
                    temperature++;
                    markDirty();
                }
            } else {
                if (temperature > 0) {
                    temperature--;
                    markDirty();
                }
            }
        }

        contents.tick(world);
    }

    @Override
    public Resovoir getPrimaryTank() {
        return contents instanceof Processable.Context c ? c.getPrimaryTank() : Resovoir.EMPTY;
    }

    @Override
    public Resovoir getTankOnSide(Direction direction) {
        return contents instanceof Processable.Context c ? c.getTankOnSide(direction) : Resovoir.EMPTY;
    }

    @Override
    public List<Resovoir> getAuxiliaryTanks() {
        return contents instanceof Processable.Context c ? c.getAuxiliaryTanks() : List.of();
    }

    @Override
    public void clear() {
        contents = new EmptyContents(this);
        container = ItemStack.EMPTY;
    }

    @Override
    public void writeNbt(NbtCompound compound, WrapperLookup lookup) {
        super.writeNbt(compound, lookup);
        ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, container).result().ifPresent(container -> compound.put("container", container));
        compound.putString("contentsType", contents.getId().toString());
        compound.put("contents", contents.toNbt(lookup));
    }

    @Override
    public void readNbt(NbtCompound compound, WrapperLookup lookup) {
        super.readNbt(compound, lookup);
        container = ItemStack.OPTIONAL_CODEC
                .decode(NbtOps.INSTANCE, compound.get("container"))
                .result()
                .map(Pair::getFirst)
                .orElse(ItemStack.EMPTY);
        contents = Contents.TYPES
                .getOrDefault(Identifier.of(compound.getString("contentsType")), Contents.TYPES.get(EmptyContents.ID))
                .create(this, compound.getCompound("contents"), lookup);
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return side.getAxis() == Axis.Y ? CONTAINER_SLOT_ID : contents instanceof SidedInventory l ? l.getAvailableSlots(side) : new int[0];
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, Direction dir) {
        if (slot == CONTAINER_SLOT_ID[0] && dir == Direction.UP) {
            return container.isEmpty() && FluidCapacity.get(stack) > 0;
        }
        return contents instanceof SidedInventory l && l.canInsert(slot - 1, stack, dir);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        if (slot == CONTAINER_SLOT_ID[0] && container.isEmpty()) {
            return false;
        }
        return contents instanceof SidedInventory l && l.canExtract(slot - 1, stack, dir);
    }

    @Override
    public int size() {
        return 1 + (contents instanceof Inventory i ? i.size() : 0);
    }

    @Override
    public boolean isEmpty() {
        return container.isEmpty() && contents.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot == 0) {
            return contents.getFilled(container, true, 1);
        }
        return contents instanceof Inventory l ? l.getStack(slot - 1) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        if (slot == 0) {
            float drainPercentage = container.getCount() / (float)amount;
            return contents.getFilled(container.split(amount), false, drainPercentage);
        }
        return contents instanceof Inventory l ? l.removeStack(slot - 1, amount) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot) {
        if (slot == 0) {
            return contents.getFilled(container.split(container.getCount()), false, 1);
        }
        return contents instanceof Inventory l ? l.removeStack(slot - 1) : ItemStack.EMPTY;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot == 0) {
            container = stack.split(1);
            contents = new EmptyContents(this).getForStack(getWorld(), getPos(), getCachedState(), stack);
        } else if (contents instanceof Inventory l) {
            l.setStack(slot - 1, stack);
        }
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public List<ItemStack> getDroppedStacks(ItemStack ignored) {
        if (contents instanceof DirectionalFluidResovoir l) {
            return l.getDroppedStacks(getContainer());
        }
        if (!getContainer().isEmpty()) {
            return List.of(getContainer());
        }
        return List.of();
    }

    public interface Contents extends NbtSerialisable, PipeInsertable {
        Map<Identifier, Factory> TYPES = Util.make(new HashMap<>(), map -> {
            map.put(EmptyContents.ID, (entity, nbt, lookup) -> new EmptyContents(entity));
            map.put(SmallContents.ID, SmallContents::new);
            map.put(LargeContents.ID, LargeContents::new);
        });

        Identifier getId();

        void tick(ServerWorld world);

        boolean isEmpty();

        ItemStack getFilled(ItemStack container, boolean dryRun, float drainPercentage);

        @Nullable
        TypedActionResult<Contents> interact(ItemStack stack, PlayerEntity player, Hand hand, Direction side);

        interface Factory {
            Contents create(BurnerBlockEntity entity, NbtCompound compound, WrapperLookup lookup);
        }
    }
}
