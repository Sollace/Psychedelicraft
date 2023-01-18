/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity;

import ivorius.psychedelicraft.fluid.*;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;

public abstract class FluidProcessingBlockEntity extends FlaskBlockEntity {
    private final Processable.ProcessType processType;

    protected int timeProcessed;
    private int timeNeeded;

    public final PropertyDelegate propertyDelegate = new PropertyDelegate(){
        @Override
        public int get(int index) {
            switch (index) {
                case 0: {
                    return timeNeeded;
                }
                case 1: {
                    return timeProcessed;
                }
            }
            return 0;
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0: {
                    timeNeeded = value;
                    break;
                }
                case 1: {
                    timeProcessed = value;
                    break;
                }
            }
        }

        @Override
        public int size() {
            return 2;
        }
    };

    public int getNeededTime() {
        return timeNeeded;
    }

    public float getProgress() {
        if (isActive()) {
            return (float)timeProcessed / timeNeeded;
        }
        return 0;
    }

    public boolean isActive() {
        return timeNeeded != Processable.UNCONVERTABLE;
    }

    public FluidProcessingBlockEntity(
            BlockEntityType<? extends FluidProcessingBlockEntity> type,
            BlockPos pos, BlockState state, int capacity,
            Processable.ProcessType processType) {
        super(type, pos, state, capacity);
        this.processType = processType;
    }

    protected boolean isOpen() {
        return false;
    }

    @Override
    public void tick(ServerWorld world) {
        super.tick(world);

        Resovoir tank = getTank(Direction.UP);
        if (tank.getFluidType() instanceof Processable p) {
            boolean open = isOpen();
            timeNeeded = p.getProcessingTime(tank.getStack(), processType, open);

            if (canProcess(world, timeNeeded)) {
                if (timeProcessed >= timeNeeded) {
                    onProcessCompleted(world, tank, p.process(tank.getStack(), processType, open));
                } else {
                    timeProcessed++;
                }
            } else {
                timeProcessed = 0;
            }
        }
    }

    protected boolean canProcess(ServerWorld world, int timeNeeded) {
        return timeNeeded >= 0;
    }

    protected void onProcessCompleted(ServerWorld world, Resovoir tank, ItemStack solids) {
        timeProcessed = 0;
        timeNeeded = Processable.UNCONVERTABLE;

        world.getChunkManager().markForUpdate(getPos());
        markDirty();
    }

    @Override
    public void onDrain(Resovoir resovoir) {
        if (resovoir.isEmpty()) {
            timeProcessed = 0;
        }
        super.onDrain(resovoir);
    }

    @Override
    public void onFill(Resovoir resovoir, int amountFilled) {
        super.onFill(resovoir, amountFilled);
        double percentFilled = amountFilled / (double) resovoir.getLevel();
        timeProcessed = MathHelper.floor(timeProcessed * (1 - percentFilled));
    }

    @Override
    public void writeNbt(NbtCompound compound) {
        super.writeNbt(compound);
        compound.putInt("timeProcessed", timeProcessed);
    }

    @Override
    public void readNbt(NbtCompound compound) {
        super.readNbt(compound);
        timeProcessed = compound.getInt("timeProcessed");
    }
}
