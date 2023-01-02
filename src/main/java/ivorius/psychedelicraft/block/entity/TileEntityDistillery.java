/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity;

import ivorius.psychedelicraft.fluids.*;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;

/**
 * Created by lukas on 25.10.14.
 */
public class TileEntityDistillery extends FlaskBlockEntity {
    public static final int DISTILLERY_CAPACITY = FlaskBlockEntity.FLASK_CAPACITY;

    public int direction;
    public int timeDistilled;

    public TileEntityDistillery(BlockPos pos, BlockState state) {
        super(PSBlockEntities.DISTILLERY, pos, state, DISTILLERY_CAPACITY);
    }

    @Override
    public void tick(ServerWorld world) {
        super.tick(world);
        Resovoir tank = getTank(Direction.UP);
        SimpleFluid fluidStack = tank.getFluidType();

        if (fluidStack instanceof FluidDistillable distillable) {
            int neededDistillationTime = distillable.distillationTime(tank.getStack());

            if (neededDistillationTime >= 0 && world.getBlockEntity(getPos().east()) instanceof FlaskBlockEntity destination) {
                if (timeDistilled >= neededDistillationTime)
                {
                    ItemStack leftover = distillable.distillStep(tank.getStack());
                    ItemStack overflow = destination.getTank(Direction.WEST).deposit(leftover);
                    // TODO: (Sollace) Overflow needs to go somewhere. Original didn't do anything with it either.
                    timeDistilled = 0;
                    world.getChunkManager().markForUpdate(getPos());
                    markDirty();
                }
                else
                    timeDistilled++;
            }
        }
    }

    @Override
    public void onDrain(Resovoir resovoir) {
        if (resovoir.isEmpty()) {
            timeDistilled = 0;
        }
        super.onDrain(resovoir);
    }

    @Override
    public void onFill(Resovoir resovoir, int amountFilled) {
        super.onFill(resovoir, amountFilled);
        double percentFilled = amountFilled / (double) resovoir.getLevel();
        timeDistilled = MathHelper.floor(timeDistilled * (1 - percentFilled));
    }

    public int getNeededDistillationTime() {
        Resovoir tank = getTank(Direction.UP);
        SimpleFluid fluid = tank.getFluidType();
        if (fluid instanceof FluidDistillable distillable) {
            return distillable.distillationTime(tank.getStack());
        }

        return Fermentable.UNFERMENTABLE;
    }

    public int getRemainingDistillationTimeScaled(int scale) {
        int neededDistillationTime = getNeededDistillationTime();
        if (neededDistillationTime >= 0)
            return (neededDistillationTime - timeDistilled) * scale / neededDistillationTime;

        return scale;
    }

    public boolean isDistilling() {
        return getNeededDistillationTime() >= 0;
    }

    @Override
    public void writeNbt(NbtCompound compound) {
        super.writeNbt(compound);
        compound.putInt("timeDistilled", timeDistilled);
    }

    @Override
    public void readNbt(NbtCompound compound) {
        super.readNbt(compound);
        timeDistilled = compound.getInt("timeDistilled");
    }
}
