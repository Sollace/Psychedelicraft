/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity;

import static ivorius.psychedelicraft.fluid.FluidHelper.MILLIBUCKETS_PER_LITER;

import ivorius.psychedelicraft.fluid.*;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;

public class BarrelBlockEntity extends FluidProcessingBlockEntity {
    public static final int BARREL_CAPACITY = MILLIBUCKETS_PER_LITER * 16;

    public int timeFermented;

    public float tapRotation = 0.0f;
    public int timeLeftTapOpen = 0;

    public BarrelBlockEntity(BlockPos pos, BlockState state) {
        super(PSBlockEntities.BARREL, pos, state, BARREL_CAPACITY, Processable.ProcessType.FERMENT);
    }

    @Override
    public void tick(ServerWorld world) {
        super.tick(world);

        if (timeLeftTapOpen > 0) {
            timeLeftTapOpen--;
        }

        if (timeLeftTapOpen > 0 && tapRotation < 3.141f * 0.5f) {
            tapRotation += 3.141f * 0.1f;
        }

        if (timeLeftTapOpen == 0 && tapRotation > 0.0f) {
            tapRotation -= 3.141f * 0.1f;
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
