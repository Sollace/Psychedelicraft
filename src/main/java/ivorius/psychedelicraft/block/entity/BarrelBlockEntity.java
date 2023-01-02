/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;

import static ivorius.psychedelicraft.fluids.FluidHelper.MILLIBUCKETS_PER_LITER;

import ivorius.psychedelicraft.fluids.*;

public class BarrelBlockEntity extends FlaskBlockEntity {
    public static final int BARREL_CAPACITY = MILLIBUCKETS_PER_LITER * 16;

    public int timeFermented;

    public float tapRotation = 0.0f;
    public int timeLeftTapOpen = 0;

    public BarrelBlockEntity(BlockPos pos, BlockState state) {
        super(PSBlockEntities.BARREL, pos, state, BARREL_CAPACITY);
    }

    @Override
    public void tick(ServerWorld world) {
        super.tick(world);

        Resovoir tank = getTank(Direction.UP);
        SimpleFluid fluid = tank.getFluidType();
        if (fluid instanceof Fermentable fermentable) {
            int neededFermentationTime = fermentable.getFermentationTime(tank.getStack(), false);

            if (neededFermentationTime >= 0) {
                if (timeFermented >= neededFermentationTime) {
                    fermentable.ferment(tank.getStack(), false);
                    timeFermented = 0;

                    world.getChunkManager().markForUpdate(getPos());
                    markDirty();
                } else {
                    timeFermented++;
                }
            }
        }

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
    public void onDrain(Resovoir resovoir) {
        if (resovoir.isEmpty()) {
            timeFermented = 0;
        }
        super.onDrain(resovoir);
    }

    @Override
    public void onFill(Resovoir resovoir, int amountFilled) {
        super.onFill(resovoir, amountFilled);
        double percentFilled = amountFilled / (double) resovoir.getLevel();
        timeFermented = MathHelper.floor(timeFermented * (1 - percentFilled));
    }

    public int getNeededFermentationTime() {
        Resovoir tank = getTank(Direction.UP);
        SimpleFluid fluid = tank.getFluidType();
        if (fluid instanceof Fermentable fermentable) {
            return fermentable.getFermentationTime(tank.getStack(), false);
        }

        return Fermentable.UNFERMENTABLE;
    }

    public int getRemainingFermentationTimeScaled(int scale) {
        int neededFermentationTime = getNeededFermentationTime();
        if (neededFermentationTime >= 0) {
            return (neededFermentationTime - timeFermented) * scale / neededFermentationTime;
        }
        return scale;
    }

    public boolean isFermenting() {
        return getNeededFermentationTime() >= 0;
    }

    @Override
    public void writeNbt(NbtCompound compound) {
        super.writeNbt(compound);
        compound.putInt("timeFermented", timeFermented);
        compound.putInt("timeLeftTapOpen", timeLeftTapOpen);
        compound.putFloat("tapRotation", tapRotation);
    }

    @Override
    public void readNbt(NbtCompound compound) {
        super.readNbt(compound);
        timeFermented = compound.getInt("timeFermented");
        timeLeftTapOpen = compound.getInt("timeLeftTapOpen");
        tapRotation = compound.getFloat("tapRotation");
    }
}
