/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity;

import ivorius.psychedelicraft.fluid.*;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.*;

public class BarrelBlockEntity extends FluidProcessingBlockEntity {

    public int timeFermented;

    public float tapRotation = 0;
    public int timeLeftTapOpen = 0;

    public BarrelBlockEntity(BlockPos pos, BlockState state) {
        super(PSBlockEntities.BARREL, pos, state, FluidVolumes.BARREL, Processable.ProcessType.FERMENT);
    }

    @Override
    protected FluidContainer getContainerType() {
        return FluidContainer.of(getCachedState().getBlock().asItem());
    }

    @Override
    public void tick(ServerWorld world) {
        super.tick(world);
        tickAnimations();
    }

    public void tickAnimations() {
        if (timeLeftTapOpen > 0) {
            timeLeftTapOpen--;
        }

        if (timeLeftTapOpen > 0 && tapRotation < MathHelper.HALF_PI) {
            tapRotation += MathHelper.PI * 0.1F;
        }

        if (timeLeftTapOpen == 0 && tapRotation > 0) {
            tapRotation -= MathHelper.PI * 0.1F;
        }

        if (timeLeftTapOpen > 0 && timeLeftTapOpen % 5 == 0) {
            world.playSound(null, getPos(), SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.BLOCKS, 0.025F, 0.5F);
        }
    }

    @Override
    public void writeNbt(NbtCompound compound) {
        super.writeNbt(compound);
        compound.putInt("timeLeftTapOpen", timeLeftTapOpen);
        compound.putFloat("tapRotation", tapRotation);
    }

    @Override
    public void readNbt(NbtCompound compound) {
        super.readNbt(compound);
        timeLeftTapOpen = compound.getInt("timeLeftTapOpen");
        tapRotation = compound.getFloat("tapRotation");
    }
}
