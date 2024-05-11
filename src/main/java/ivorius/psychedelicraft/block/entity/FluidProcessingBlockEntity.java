/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity;

import ivorius.psychedelicraft.fluid.*;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;

public abstract class FluidProcessingBlockEntity extends FlaskBlockEntity implements Processable.ByProductConsumer {
    public FluidProcessingBlockEntity(
            BlockEntityType<? extends FluidProcessingBlockEntity> type,
            BlockPos pos, BlockState state, int capacity) {
        super(type, pos, state, capacity);
    }

    @Override
    protected int getTotalProperties() {
        return super.getTotalProperties() + 2;
    }

    public int getTimeNeeded() {
        return propertyDelegate.get(4);
    }

    public void setTimeNeeded(int value) {
        propertyDelegate.set(4, value);
    }

    public int getTimeProcessed() {
        return propertyDelegate.get(5);
    }

    public void setTimeProcessed(int value) {
        propertyDelegate.set(5, value);
    }

    public float getProgress() {
        if (isActive()) {
            return (float)getTimeProcessed() / getTimeNeeded();
        }
        return 0;
    }

    public abstract Processable.ProcessType getProcessType();

    public Processable.ProcessType getActiveProcess() {
        Resovoir tank = getPrimaryTank();

        if (isActive() && tank.getFluidType() instanceof Processable p) {
            return p.modifyProcess(tank, getProcessType());
        }
        return Processable.ProcessType.IDLE;
    }

    public boolean isActive() {
        return getTimeNeeded() != Processable.UNCONVERTABLE;
    }

    @Override
    public void tick(ServerWorld world) {
        super.tick(world);

        Resovoir tank = getPrimaryTank();

        if (tank.getFluidType() instanceof Processable p) {
            Processable.ProcessType type = p.modifyProcess(tank, getProcessType());
            setTimeNeeded(p.getProcessingTime(tank, type));

            if (canProcess(world, getTimeNeeded())) {
                if (getTimeProcessed() >= getTimeNeeded()) {
                    p.process(tank, type, this);
                    onProcessCompleted(world, tank);
                } else {
                    setTimeProcessed(getTimeProcessed() + 1);
                }
            } else {
                setTimeProcessed(0);
            }
        } else {
            setTimeNeeded(Processable.UNCONVERTABLE);
        }
    }

    protected boolean canProcess(ServerWorld world, int timeNeeded) {
        return timeNeeded >= 0;
    }

    protected void onProcessCompleted(ServerWorld world, Resovoir tank) {
        setTimeProcessed(0);
        setTimeNeeded(Processable.UNCONVERTABLE);

        markForUpdate();
    }

    @Override
    public void onDrain(Resovoir resovoir) {
        if (resovoir.isEmpty()) {
            setTimeProcessed(0);
        }
        super.onDrain(resovoir);
    }

    @Override
    public void onFill(Resovoir resovoir, int amountFilled) {
        super.onFill(resovoir, amountFilled);
        double percentFilled = amountFilled / (double) resovoir.getLevel();
        setTimeNeeded(MathHelper.floor(getTimeProcessed() * (1 - percentFilled)));
    }

    @Override
    public void writeNbt(NbtCompound compound) {
        super.writeNbt(compound);
        compound.putInt("timeProcessed", getTimeProcessed());
        compound.putInt("timeNeeded", getTimeNeeded());
    }

    @Override
    public void readNbt(NbtCompound compound) {
        super.readNbt(compound);
        setTimeProcessed(compound.getInt("timeProcessed"));
        setTimeNeeded(compound.getInt("timeNeeded"));
    }
}
