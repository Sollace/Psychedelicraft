/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity;

import ivorius.psychedelicraft.fluid.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;

/**
 * Created by lukas on 27.10.14.
 */
public class MashTubBlockEntity extends FluidProcessingBlockEntity {
    public static final int MASH_TUB_CAPACITY = FluidHelper.MILLIBUCKETS_PER_LITER * 16;

    public ItemStack solidContents = ItemStack.EMPTY;

    public MashTubBlockEntity(BlockPos pos, BlockState state) {
        super(PSBlockEntities.MASH_TUB, pos, state, MASH_TUB_CAPACITY, Processable.ProcessType.FERMENT);
    }

    @Override
    protected boolean isOpen() {
        return true;
    }

    @Override
    protected void onProcessCompleted(ServerWorld world, Resovoir tank, ItemStack solids) {
        if (!solids.isEmpty()) {
            tank.clear();
            solidContents = solids;
        }
        super.onProcessCompleted(world, tank, solids);
    }

    @Override
    public void onDestroyed(ServerWorld world) {
        super.onDestroyed(world);
        if (!solidContents.isEmpty()) {
            Block.dropStack(world, pos, solidContents);
        }
    }

    @Override
    public void onDrain(Resovoir resovoir) {
        if (!solidContents.isEmpty() && resovoir.isEmpty()) {
            timeProcessed = 0;
        }
        onIdle(resovoir);
    }

    @Override
    public void onFill(Resovoir resovoir, int amountFilled) {
        if (!solidContents.isEmpty()) {
            super.onFill(resovoir, amountFilled);
        } else {
            onIdle(resovoir);
        }
    }

    @Override
    public void writeNbt(NbtCompound compound) {
        super.writeNbt(compound);
        if (!solidContents.isEmpty()) {
            compound.put("solidContents", solidContents.writeNbt(new NbtCompound()));
        }
    }

    @Override
    public void readNbt(NbtCompound compound) {
        super.readNbt(compound);
        solidContents = compound.contains("solidContents", NbtElement.COMPOUND_TYPE)
                ? ItemStack.fromNbt(compound.getCompound("solidContents"))
                : ItemStack.EMPTY;
    }
}
