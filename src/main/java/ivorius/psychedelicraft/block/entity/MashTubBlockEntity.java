/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;

import static ivorius.psychedelicraft.fluids.FluidHelper.MILLIBUCKETS_PER_LITER;

import ivorius.psychedelicraft.fluids.*;

/**
 * Created by lukas on 27.10.14.
 */
public class MashTubBlockEntity extends FlaskBlockEntity {
    public static final int MASH_TUB_CAPACITY = MILLIBUCKETS_PER_LITER * 16;

    public int timeFermented;

    public ItemStack solidContents;

    public MashTubBlockEntity(BlockPos pos, BlockState state) {
        super(PSBlockEntities.MASH_TUB, pos, state, MASH_TUB_CAPACITY);
    }

    @Override
    public void tick(ServerWorld world) {
        super.tick(world);

        Resovoir tank = getTank(Direction.UP);
        if (tank.getFluidType() instanceof Fermentable fermentable) {
            int neededFermentationTime = fermentable.getFermentationTime(tank.getStack(), true);

            if (neededFermentationTime >= 0 && timeFermented >= neededFermentationTime) {
                ItemStack solid = fermentable.ferment(tank.getStack(), true);
                timeFermented = 0;

                if (!solid.isEmpty()) {
                    tank.clear();
                    solidContents = solid;
                }

                world.getChunkManager().markForUpdate(getPos());
                markDirty();
            } else {
                timeFermented++;
            }
        }
    }


    @Override
    public void onDrain(Resovoir resovoir) {
        if (!solidContents.isEmpty() && resovoir.isEmpty()) {
            timeFermented = 0;
        }
        super.onDrain(resovoir);
    }

    @Override
    public void onFill(Resovoir resovoir, int amountFilled) {
        super.onFill(resovoir, amountFilled);
        if (!solidContents.isEmpty()) {
            double percentFilled = amountFilled / (double) resovoir.getLevel();
            timeFermented = MathHelper.floor(timeFermented * (1 - percentFilled));
        }
    }

    public int getNeededFermentationTime() {
        Resovoir tank = getTank(Direction.UP);
        SimpleFluid fluid = tank.getFluidType();
        if (fluid instanceof Fermentable fermentable) {
            return fermentable.getFermentationTime(tank.getStack(), true);
        }

        return Fermentable.UNFERMENTABLE;
    }

    public int getRemainingFermentationTimeScaled(int scale) {
        int neededFermentationTime = getNeededFermentationTime();
        if (neededFermentationTime >= 0)
            return (neededFermentationTime - timeFermented) * scale / neededFermentationTime;

        return scale;
    }

    public boolean isFermenting() {
        return getNeededFermentationTime() >= 0;
    }

    @Override
    public void writeNbt(NbtCompound compound) {
        super.writeNbt(compound);
        compound.putInt("timeFermented", timeFermented);
        if (!solidContents.isEmpty()) {
            compound.put("solidContents", solidContents.writeNbt(new NbtCompound()));
        }
    }

    @Override
    public void readNbt(NbtCompound compound) {
        super.readNbt(compound);
        timeFermented = compound.getInt("timeFermented");
        solidContents = compound.contains("solidContents", NbtElement.COMPOUND_TYPE)
                ? ItemStack.fromNbt(compound.getCompound("solidContents"))
                : ItemStack.EMPTY;
    }
}
