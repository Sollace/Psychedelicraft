/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity;

import ivorius.psychedelicraft.fluid.*;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

public class BarrelBlockEntity extends FluidProcessingBlockEntity {

    public int timeFermented;

    private float prevTapRotation;
    private float tapRotation;

    public BarrelBlockEntity(BlockPos pos, BlockState state) {
        super(PSBlockEntities.BARREL, pos, state, FluidVolumes.BARREL);
    }

    @Override
    protected int getTotalProperties() {
        return super.getTotalProperties() + 1;
    }

    @Override
    public Processable.ProcessType getProcessType() {
        return Processable.ProcessType.MATURE;
    }

    @Override
    public void clientTick(World world) {
        super.clientTick(world);

        prevTapRotation = tapRotation;

        if (getTapOpenTicks() > 0 && tapRotation < MathHelper.HALF_PI) {
            tapRotation += MathHelper.PI * 0.1F;
        }

        if (getTapOpenTicks() == 0 && tapRotation > 0) {
            tapRotation -= MathHelper.PI * 0.1F;
        }
    }

    @Override
    public void tick(ServerWorld world) {
        super.tick(world);
        if (getTapOpenTicks() > 0) {
            setTapOpenTicks(getTapOpenTicks() - 1);
            markForUpdate();
        }
        if (getTapOpenTicks() > 0 && getTapOpenTicks() % 5 == 0) {
            world.playSound(null, getPos(), SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.BLOCKS, 0.025F, 0.5F);
        }
    }

    public int getTapOpenTicks() {
        return propertyDelegate.get(6);
    }

    public void setTapOpenTicks(int ticks) {
        propertyDelegate.set(6, ticks);
    }

    public float getTapRotation(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevTapRotation, tapRotation);
    }

    @Override
    public void writeNbt(NbtCompound compound, WrapperLookup lookup) {
        super.writeNbt(compound, lookup);
        compound.putInt("timeLeftTapOpen", getTapOpenTicks());
    }

    @Override
    public void readNbt(NbtCompound compound, WrapperLookup lookup) {
        super.readNbt(compound, lookup);
        setTapOpenTicks(compound.getInt("timeLeftTapOpen"));
    }
}
