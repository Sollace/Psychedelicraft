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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;

import ivorius.psychedelicraft.block.BlockWithFluid;
import ivorius.psychedelicraft.fluid.*;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.util.NbtSerialisable;

/**
 * Created by lukas on 25.10.14.
 * Updated by Sollace on 2 Jan 2023
 */
public class FlaskBlockEntity extends SyncedBlockEntity implements BlockWithFluid.DirectionalFluidResovoir, Resovoir.ChangeListener {
    private static final int[] NO_SLOT_ID = {};
    private static final int[] INPUT_SLOT_ID = {0};
    private static final int[] OUTPUT_SLOT_ID = {1};

    private final Resovoir tank;
    private boolean pendingSync;

    public final IoSlot inputSlot = new IoSlot(0);
    public final IoSlot outputSlot = new IoSlot(1);

    public final IoInventory ioInventory = new IoInventory();

    public final PropertyDelegate propertyDelegate = new ArrayPropertyDelegate(getTotalProperties());

    public FlaskBlockEntity(BlockPos pos, BlockState state) {
        this(PSBlockEntities.FLASK, pos, state, FluidVolumes.FLASK);
    }

    public FlaskBlockEntity(BlockEntityType<? extends FlaskBlockEntity> type, BlockPos pos, BlockState state, int capacity) {
        super(type, pos, state);
        this.tank = new Resovoir(capacity, this);
    }

    public void markForUpdate() {
        pendingSync = true;
    }

    protected int getTotalProperties() {
        return 4;
    }

    @Override
    public void onLevelChange(Resovoir resovoir, int change) {
        markForUpdate();
    }

    @Override
    public Resovoir getPrimaryTank() {
        return tank;
    }

    @Override
    public void tick(ServerWorld world) {
        ItemStack output = outputSlot.getStack();
        boolean playSound = false;

        if (FluidCapacity.get(output) > 0) {
            ItemFluids.Transaction t = ItemFluids.Transaction.begin(output);
            int amount = tank.withdraw(t, FluidVolumes.BOWL);
            if (amount > 0) {
                outputSlot.incrementLevelsTransferred(amount);
                playSound = true;
                outputSlot.setStack(t.toItemStack());
            }
        }

        ItemStack input = inputSlot.getStack();
        if (FluidCapacity.get(input) > 0) {
            ItemFluids.Transaction t = ItemFluids.Transaction.begin(input);

            int amount = tank.deposit(t, FluidVolumes.BOWL);
            if (amount > 0) {
                inputSlot.incrementLevelsTransferred(amount);
                inputSlot.setStack(t.toItemStack());
                playSound = true;
            }
        }

        if (playSound && world.getTime() % 9 == 0) {
            world.playSound(null, getPos(), SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.BLOCKS, 0.025F, 0.5F);
        }

        if (pendingSync) {
            pendingSync = false;
            markDirty();
            world.getChunkManager().markForUpdate(getPos());
        }
    }

    @Deprecated
    @Override
    public List<ItemStack> getDroppedStacks(ItemStack container) {
        List<ItemStack> stacks = new ArrayList<>();
        int maxCapacity = Math.min(FluidCapacity.get(container), tank.getContents().amount());
        if (maxCapacity > 0) {
            stacks.add(ItemFluids.set(container, tank.getContents().ofAmount(maxCapacity)));
        }
        return stacks;
    }

    @Override
    public void writeNbt(NbtCompound compound, WrapperLookup lookup) {
        super.writeNbt(compound, lookup);
        compound.put("tank", tank.toNbt(lookup));
        Inventories.writeNbt(compound, ioInventory.heldStacks, lookup);
        compound.put("inputSlot", inputSlot.toNbt(lookup));
        compound.put("outputSlot", outputSlot.toNbt(lookup));
    }

    @Override
    public void readNbt(NbtCompound compound, WrapperLookup lookup) {
        super.readNbt(compound, lookup);
        tank.fromNbt(compound.getCompound("tank"), lookup);
        Inventories.readNbt(compound, ioInventory.heldStacks, lookup);
        inputSlot.fromNbt(compound.getCompound("inputSlot"), lookup);
        outputSlot.fromNbt(compound.getCompound("outputSlot"), lookup);
    }

    @Override
    public int size() {
        return ioInventory.size();
    }

    @Override
    public boolean isEmpty() {
        return ioInventory.isEmpty() && tank.getContents().isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return ioInventory.getStack(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return ioInventory.removeStack(slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return ioInventory.removeStack(slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        ioInventory.setStack(slot, stack);
        onContentsExternallyChanged(slot);
    }

    public void onContentsExternallyChanged(int slot) {
        if (slot == 0) {
            inputSlot.onChange();
        }
        if (slot == 1) {
            outputSlot.onChange();
        }
    }

    @Override
    public boolean canPlayerUse(PlayerEntity var1) {
        return true;
    }

    @Override
    public void clear() {
        ioInventory.clear();
        tank.clear();
    }

    @Override
    public int[] getAvailableSlots(Direction direction) {
        Direction facing = getCachedState().getOrEmpty(Properties.HORIZONTAL_FACING).orElse(Direction.UP);
        if (facing.getAxis() != Direction.Axis.Y && direction.getAxis() != Direction.Axis.Y) {
            direction = Direction.fromRotation(facing.asRotation() - direction.asRotation());
        }

        if (direction == Direction.EAST) {
            return INPUT_SLOT_ID;
        }
        if (direction == Direction.WEST) {
            return OUTPUT_SLOT_ID;
        }
        if (direction == Direction.UP || direction == Direction.DOWN) {
            return new int[] {0, 1};
        }
        return NO_SLOT_ID;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, Direction direction) {
        int[] availableSlots = getAvailableSlots(direction);

        if (availableSlots.length == 0) {
            return false;
        }

        if (slot == OUTPUT_SLOT_ID[0]) {
            return ioInventory.isValid(slot, stack) && FluidCapacity.getPercentage(stack) < 1;
        }

        if (slot == INPUT_SLOT_ID[0]) {
            return ioInventory.isValid(slot, stack) && FluidCapacity.getPercentage(stack) > 0;
        }

        return false;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction direction) {
        int[] availableSlots = getAvailableSlots(direction);

        if (availableSlots.length == 0) {
            return false;
        }

        if (slot == OUTPUT_SLOT_ID[0] && outputSlot.getFillPercentage(-1) >= 1) {
            return true;
        }

        if (slot == INPUT_SLOT_ID[0] && inputSlot.getFillPercentage(1) <= 0) {
            return true;
        }

        return false;
    }

    class IoInventory extends SimpleInventory {
        public IoInventory() {
            super(2);
        }

        @Override
        public int getMaxCountPerStack() {
            return 1;
        }

        @Override
        public boolean isValid(int slot, ItemStack stack) {
            return slot == 0 ? FluidCapacity.getPercentage(stack) > 0 : FluidCapacity.getPercentage(stack) < 1;
        }
    }

    public class IoSlot implements NbtSerialisable {
        public final int index;

        private final int levelsTransferredIndex;
        private final int inputtedLevelsIndex;

        public IoSlot(int index) {
            this.index = index;
            levelsTransferredIndex = 0 + (index * 2);
            inputtedLevelsIndex = 1 + (index * 2);
        }

        public float getProgress() {
            return propertyDelegate.get(inputtedLevelsIndex) == 0 ? 0 : (float)propertyDelegate.get(levelsTransferredIndex) / propertyDelegate.get(inputtedLevelsIndex);
        }

        public void incrementLevelsTransferred(int amount) {
            propertyDelegate.set(levelsTransferredIndex, propertyDelegate.get(levelsTransferredIndex) + amount);
        }

        public void onChange() {
            ItemStack stack = ioInventory.getStack(index);
            var fluids = ItemFluids.of(stack);
            int levels = fluids.amount();
            if (index == 1) {
                levels = FluidCapacity.get(stack) - levels;
            }
            propertyDelegate.set(inputtedLevelsIndex, levels);
            propertyDelegate.set(levelsTransferredIndex, 0);
        }

        public ItemStack getStack() {
            return ioInventory.getStack(index);
        }

        public void setStack(ItemStack stack) {
            ioInventory.setStack(index, stack);
        }

        public float getFillPercentage(float def) {
            return FluidCapacity.getPercentage(getStack());
        }

        @Override
        public void toNbt(NbtCompound compound, WrapperLookup lookup) {
            compound.putInt("inputtedLevels", propertyDelegate.get(inputtedLevelsIndex));
            compound.putInt("levelsTransferred", propertyDelegate.get(levelsTransferredIndex));
        }

        @Override
        public void fromNbt(NbtCompound compound, WrapperLookup lookup) {
            propertyDelegate.set(inputtedLevelsIndex, compound.getInt("inputtedLevels"));
            propertyDelegate.set(levelsTransferredIndex, compound.getInt("levelsTransferred"));
        }
    }
}
