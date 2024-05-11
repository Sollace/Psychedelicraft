/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity;

import ivorius.psychedelicraft.block.DistilleryBlock;
import ivorius.psychedelicraft.block.MashTubWallBlock;
import ivorius.psychedelicraft.fluid.*;
import ivorius.psychedelicraft.fluid.container.FluidContainer;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.util.math.Direction.Axis;

/**
 * Created by lukas on 25.10.14.
 */
public class DistilleryBlockEntity extends FluidProcessingBlockEntity {
    public static final int DISTILLERY_CAPACITY = FlaskBlockEntity.FLASK_CAPACITY;

    public DistilleryBlockEntity(BlockPos pos, BlockState state) {
        super(PSBlockEntities.DISTILLERY, pos, state, DISTILLERY_CAPACITY, Processable.ProcessType.DISTILL);
    }

    @Override
    protected boolean canProcess(ServerWorld world, int timeNeeded) {
        return super.canProcess(world, timeNeeded)
                && getFacing().getAxis() != Axis.Y
                && DistilleryBlock.canConnectTo(world.getBlockState(getOutputPos()), getFacing())
                && getOutput(world, getPos()) instanceof FlaskBlockEntity;
    }

    @Override
    protected void onProcessCompleted(ServerWorld world, Resovoir tank, ItemStack results) {
        world.spawnParticles(ParticleTypes.CLOUD,
                pos.getX() + world.getRandom().nextTriangular(0.5F, 0.5F),
                pos.getY() + 0.6F,
                pos.getZ() + world.getRandom().nextTriangular(0.5F, 0.5F),
                2, 0, 0, 0, 0);
        if (world.getRandom().nextInt(10) == 0) {
            world.playSound(null, getPos(), SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.BLOCKS, 0.25F, 0.02F);
        }

        BlockPos outputPos = getOutputPos();
        if (getOutput(world, getPos()) instanceof FlaskBlockEntity destination) {
            ItemStack overflow = destination.getTank(getFacing().getOpposite()).deposit(results);
            if (FluidContainer.of(overflow).getLevel(overflow) > 0) {
                Block.dropStack(world, outputPos, overflow);
            }
        } else {
            Block.dropStack(world, outputPos, results);
        }
        super.onProcessCompleted(world, tank, results);
    }

    private BlockEntity getOutput(ServerWorld world, BlockPos pos) {
        pos = getOutputPos();
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof MashTubWallBlock) {
            pos = world.getBlockEntity(pos, PSBlockEntities.MASH_TUB_EDGE).map(p -> p.getMasterPos()).orElse(pos);
        }
        return world.getBlockEntity(pos);
    }

    private BlockPos getOutputPos() {
        return getPos().offset(getFacing());
    }

    private Direction getFacing() {
        return getCachedState().get(DistilleryBlock.FACING);
    }
}
