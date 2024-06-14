/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity;

import ivorius.psychedelicraft.block.DistilleryBlock;
import ivorius.psychedelicraft.block.MashTubWallBlock;
import ivorius.psychedelicraft.fluid.*;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.util.math.Direction.Axis;

/**
 * Created by lukas on 25.10.14.
 */
public class DistilleryBlockEntity extends FluidProcessingBlockEntity {
    public static final int DISTILLERY_CAPACITY = FluidVolumes.FLASK;

    public DistilleryBlockEntity(BlockPos pos, BlockState state) {
        super(PSBlockEntities.DISTILLERY, pos, state, DISTILLERY_CAPACITY);
    }

    @Override
    public Processable.ProcessType getProcessType() {
        return Processable.ProcessType.DISTILL;
    }

    @Override
    protected int getTickRate(ServerWorld world) {
        if (world.getFluidState(getPos().down()).isIn(FluidTags.LAVA)) {
            return 7;
        }
        BlockState below = world.getBlockState(getPos().down());
        if (below.isIn(BlockTags.FIRE) || below.isIn(BlockTags.CAMPFIRES)) {
            return 3;
        }
        return 1;
    }

    @Override
    protected boolean canProcess(ServerWorld world, int timeNeeded) {

        if (world.random.nextInt(120) == 0) {
            world.spawnParticles(ParticleTypes.CLOUD,
                    pos.getX() + world.getRandom().nextTriangular(0.5F, 0.5F),
                    pos.getY() + 0.6F,
                    pos.getZ() + world.getRandom().nextTriangular(0.5F, 0.5F),
                    2, 0, 0, 0, 0);
        }
        if (world.getRandom().nextInt(50) == 0) {
            world.playSound(null, getPos(), SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.BLOCKS, 0.25F, 0.02F);
        }

        return super.canProcess(world, timeNeeded)
                && getFacing().getAxis() != Axis.Y
                && DistilleryBlock.canConnectTo(world.getBlockState(getOutputPos()), getFacing())
                && getOutput(world, getPos()) instanceof FlaskBlockEntity;
    }

    @Override
    public void accept(ItemFluids stack) {
        if (getWorld() instanceof ServerWorld world) {
            if (getOutput(world, getPos()) instanceof FlaskBlockEntity destination) {
                int transferred = destination.getTankOnSide(getFacing().getOpposite()).deposit(stack);
                if (transferred < stack.amount()) {
                    //BlockPos outputPos = getOutputPos();
                    // TODO: Droplets item?
                    world.playSound(null, getPos(), SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, 0.25F, 0.02F);
                    world.spawnParticles(ParticleTypes.SPLASH,
                            pos.getX() + world.getRandom().nextTriangular(0.5F, 0.5F),
                            pos.getY() + 0.6F,
                            pos.getZ() + world.getRandom().nextTriangular(0.5F, 0.5F),
                            2, 0, 0, 0, 0);
                }
            } else {
                world.spawnParticles(ParticleTypes.CLOUD,
                        pos.getX() + world.getRandom().nextTriangular(0.5F, 0.5F),
                        pos.getY() + 0.6F,
                        pos.getZ() + world.getRandom().nextTriangular(0.5F, 0.5F),
                        2, 0, 0, 0, 0);
                world.playSound(null, getPos(), SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.BLOCKS, 0.25F, 0.02F);
            }
        }
    }

    @Override
    protected void onProcessCompleted(ServerWorld world, Resovoir tank) {
        world.spawnParticles(ParticleTypes.CLOUD,
                pos.getX() + world.getRandom().nextTriangular(0.5F, 0.5F),
                pos.getY() + 0.6F,
                pos.getZ() + world.getRandom().nextTriangular(0.5F, 0.5F),
                2, 0, 0, 0, 0);
        if (world.getRandom().nextInt(10) == 0) {
            world.playSound(null, getPos(), SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.BLOCKS, 0.25F, 0.02F);
        }
        super.onProcessCompleted(world, tank);
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
