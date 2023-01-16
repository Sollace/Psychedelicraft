/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import net.minecraft.block.*;
import net.minecraft.item.ItemConvertible;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.*;

public class CannabisPlantBlock extends CropBlock {
    public static final int MAX_AGE = 15;
    public static final int MATURATION_AGE = 11;

    private static final VoxelShape SHAPE = Block.createCuboidShape(2, 0, 2, 14, 16, 14);

    public CannabisPlantBlock(Settings settings) {
        super(settings.ticksRandomly().nonOpaque());
    }

    public BlockState getStateForHeight(int y) {
        return getDefaultState();
    }

    @Override
    public IntProperty getAgeProperty() {
        return Properties.AGE_15;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(getAgeProperty());
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    public int getMaxAge(BlockState state) {
        return MAX_AGE;
    }

    public int getMaxHeight() {
        return 3;
    }

    protected float getRandomGrothChance() {
        return 0.12F;
    }

    @Override
    protected ItemConvertible getSeedsItem() {
        return asItem();
    }

    @Override
    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return floor.isOf(Blocks.FARMLAND) || floor.isOf(this);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (world.getBaseLightLevel(pos.up(), 0) >= 9 && random.nextFloat() < getRandomGrothChance()) {
            if (isFertilizable(world, pos, state, false)) {
                applyGrowth(world, pos, state, false);
            }
        }
    }

    public void applyGrowth(World world, BlockPos pos, BlockState state, boolean bonemeal) {
        int number = bonemeal ? world.random.nextInt(4) + 1 : 1;

        for (int i = 0; i < number; i++) {
            final int age = state.get(getAgeProperty());
            final boolean freeOver = world.isAir(pos.up()) && getPlantSize(world, pos) < getMaxHeight();

            if ((age < getMaxAge(state) && freeOver) || (!freeOver && age < MATURATION_AGE)) {
                state = state.cycle(getAgeProperty());
                world.setBlockState(pos, state, Block.NOTIFY_ALL);
            } else if (world.isAir(pos.up()) && freeOver && age == getMaxAge(state)) {
                world.setBlockState(pos.up(), getDefaultState(), Block.NOTIFY_ALL);
            }
        }
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean client) {
        final boolean freeOver = getPlantSize(world, pos) < getMaxHeight();
        final int age = state.get(getAgeProperty());
        boolean ret = (age < getMaxAge(state) && freeOver)
            || (!freeOver && age < MATURATION_AGE)
            || (world.isAir(pos.up()) && freeOver && age == getMaxAge(state));

        return ret;
    }

    protected int getPlantSize(WorldView world, BlockPos pos) {
        int plantSize = 1;
        while (world.getBlockState(pos.down(plantSize)).isOf(this)) {
            ++plantSize;
        }
        return plantSize;
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        applyGrowth(world, pos, state, true);
    }
}
