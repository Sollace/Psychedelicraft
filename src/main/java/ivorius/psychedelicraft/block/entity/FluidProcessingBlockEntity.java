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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;

public abstract class FluidProcessingBlockEntity extends FlaskBlockEntity {
    private final Processable.ProcessType processType;

    public int timeProcessed;

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
            int timeNeeded = p.getProcessingTime(tank.getStack(), processType, open);

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

    public int getNeededTime() {
        Resovoir tank = getTank(Direction.UP);
        SimpleFluid fluid = tank.getFluidType();
        if (fluid instanceof Processable p) {
            return p.getProcessingTime(tank.getStack(), Processable.ProcessType.FERMENT, isOpen());
        }

        return Processable.UNCONVERTABLE;
    }

    public int getProgress(int scale) {
        int neededTime = getNeededTime();
        if (neededTime >= 0) {
            return (neededTime - timeProcessed) * scale / neededTime;
        }
        return scale;
    }

    public boolean isActive() {
        return getNeededTime() >= 0;
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
