/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity;

import ivorius.psychedelicraft.fluid.*;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
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
        if (value > 0 && getWorld() instanceof ServerWorld sw) {
            value = Math.max(1, value / getTickRate(sw));
        }
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

        if (isActive() && tank.getContents().fluid() instanceof Processable p) {
            return p.modifyProcess(tank, getProcessType());
        }
        return Processable.ProcessType.IDLE;
    }

    public boolean isActive() {
        return getTimeNeeded() != Processable.UNCONVERTABLE;
    }

    protected int getTickRate(ServerWorld world) {
        return 1;
    }

    @Override
    public void tick(ServerWorld world) {
        super.tick(world);

        Resovoir tank = getPrimaryTank();

        if (tank.getContents().fluid() instanceof Processable p) {
            Processable.ProcessType type = p.modifyProcess(tank, getProcessType());
            setTimeNeeded(p.getProcessingTime(tank, type));

            if (canProcess(world, getTimeNeeded())) {
                if (getTimeProcessed() >= getTimeNeeded()) {
                    p.process(this, type, this);
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
    public void onLevelChange(Resovoir resovoir, int difference) {
        if (resovoir.getContents().isEmpty()) {
            setTimeProcessed(0);
        }
        super.onLevelChange(resovoir, difference);
    }

    @Override
    public void writeNbt(NbtCompound compound, WrapperLookup lookup) {
        super.writeNbt(compound, lookup);
        compound.putInt("timeProcessed", getTimeProcessed());
        compound.putInt("timeNeeded", getTimeNeeded());
    }

    @Override
    public void readNbt(NbtCompound compound, WrapperLookup lookup) {
        super.readNbt(compound, lookup);
        setTimeProcessed(compound.getInt("timeProcessed"));
        setTimeNeeded(compound.getInt("timeNeeded"));
    }

    @Override
    public void accept(ItemStack stack) {
        Block.dropStack(world, getPos(), stack);
    }

    @Override
    public void accept(ItemFluids stack) {
        if (getWorld() instanceof ServerWorld world) {
            int transferred = getPrimaryTank().deposit(stack);
            if (transferred < stack.amount()) {
                // TODO: Droplets item?
                world.playSound(null, getPos(), SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, 0.25F, 0.02F);
                world.spawnParticles(ParticleTypes.SPLASH,
                        pos.getX() + world.getRandom().nextTriangular(0.5F, 0.5F),
                        pos.getY() + 0.6F,
                        pos.getZ() + world.getRandom().nextTriangular(0.5F, 0.5F),
                        2, 0, 0, 0, 0);
            }
        }
    }
}
