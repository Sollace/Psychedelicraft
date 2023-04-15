/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import net.minecraft.block.*;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.*;

public class CannabisPlantBlock extends CropBlock {
    public static final int MAX_AGE = 15;
    public static final int MAX_AGE_WHILE_COVERED = 11;

    private static final VoxelShape SHAPE = Block.createCuboidShape(2, 0, 2, 14, 16, 14);

    public static final BooleanProperty GROWING = BooleanProperty.of("growing");
    public static final BooleanProperty NATURAL = BooleanProperty.of("natural");

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
        builder.add(getAgeProperty(), GROWING, NATURAL);
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

    protected float getRandomGrowthChance() {
        return 0.12F;
    }

    @Override
    protected ItemConvertible getSeedsItem() {
        return asItem();
    }

    @Override
    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return floor.isOf(this) || super.canPlantOnTop(floor, world, pos);
    }

    @Override
    public final boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        if (state.get(NATURAL)) {
            BlockState floor = world.getBlockState(pos.down());
            return floor.isOf(this) || floor.isOf(Blocks.GRASS_BLOCK) || floor.isIn(BlockTags.DIRT);
        }
        return super.canPlaceAt(state, world, pos);
    }

    @Override
    public boolean isMature(BlockState state) {
        return state.get(getAgeProperty()) >= getMaxAge(state) && !state.get(GROWING);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (world.getBaseLightLevel(pos.up(), 0) >= 9 && random.nextFloat() < getRandomGrowthChance()) {
            if (isFertilizable(world, pos, state, false)) {
                applyGrowth(world, pos, state, false);
            }
        }
    }

    public void applyGrowth(World world, BlockPos pos, BlockState state, boolean bonemeal) {
        int number = bonemeal ? world.random.nextInt(4) + 1 : 1;

        for (int i = 0; i < number; i++) {
            if (state.get(getAgeProperty()) < getMaxAge(state)) {
                state = state.cycle(getAgeProperty());
                world.setBlockState(pos, state, Block.NOTIFY_ALL);
            } else if (canGrowUpwards(world, pos, state)) {
                pos = pos.up();
                state = getDefaultState();
                world.setBlockState(pos, state, Block.NOTIFY_ALL);
            }
        }
    }

    protected int getPlantSize(WorldView world, BlockPos pos) {
        int plantSize = 1;
        while (world.getBlockState(pos.down(plantSize)).isOf(this)) {
            ++plantSize;
        }
        return plantSize;
    }

    protected boolean canGrowUpwards(World world, BlockPos pos, BlockState state) {
        return world.isAir(pos.up())
                && getPlantSize(world, pos) < getMaxHeight()
                && state.get(getAgeProperty()) > MAX_AGE_WHILE_COVERED;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        applyGrowth(world, pos, state, true);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.UP) {
            return state.with(GROWING, neighborState.isAir() && getPlantSize(world, pos) < getMaxHeight());
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }
}
