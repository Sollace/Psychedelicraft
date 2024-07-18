/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import com.mojang.serialization.MapCodec;

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
    public static final MapCodec<CannabisPlantBlock> CODEC = createCodec(CannabisPlantBlock::new);
    public static final int MAX_AGE = 15;
    public static final int MAX_AGE_WHILE_COVERED = 11;

    private static final VoxelShape SHAPE = Block.createCuboidShape(2, 0, 2, 14, 16, 14);

    public static final BooleanProperty GROWING = BooleanProperty.of("growing");
    public static final BooleanProperty NATURAL = BooleanProperty.of("natural");

    public CannabisPlantBlock(Settings settings) {
        super(settings.ticksRandomly().nonOpaque());
        setDefaultState(getDefaultState().with(NATURAL, false));
    }

    @Override
    public MapCodec<? extends CannabisPlantBlock> getCodec() {
        return CODEC;
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
            if (isFertilizable(world, pos, state)) {
                applyGrowth(world, pos, state, false);
            }
        }
    }

    protected int getMaxBonemealGrowth() {
        return 4;
    }

    public void applyGrowth(World world, final BlockPos initialPos, BlockState state, boolean bonemeal) {
        int number = bonemeal ? world.random.nextInt(getMaxBonemealGrowth()) + 1 : 1;

        BlockPos.Mutable pos = initialPos.mutableCopy();
        BlockPos.Mutable up = initialPos.up().mutableCopy();

        while (number > 0) {
            int age = state.get(getAgeProperty());
            int maxAge = getMaxAge(state);
            if (age < maxAge) {
                int increment = Math.min(number, maxAge - age);
                number -= increment;
                state = state.with(getAgeProperty(), age + increment).with(GROWING, world.isAir(up) && getPlantSize(world, pos) < getMaxHeight());
                world.setBlockState(pos, state, Block.NOTIFY_ALL);
            } else {
                int y = getPlantSize(world, pos);
                if (y >= getMaxHeight()) {
                    break;
                }

                BlockState above = world.getBlockState(up);
                if (above.isOf(this)) {
                    pos.move(Direction.UP);
                    up.move(Direction.UP);
                    state = above;
                } else if (canGrowUpwards(world, pos, state)) {
                    pos.move(Direction.UP);
                    up.move(Direction.UP);
                    number--;
                    state = getStateForHeight(y + 1).with(GROWING, world.isAir(up) && getPlantSize(world, pos) < getMaxHeight());
                    world.setBlockState(pos, state, Block.NOTIFY_ALL);
                }
            }
        }
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
        if (!isMature(state)) {
            return true;
        }
        pos = pos.up();
        state = world.getBlockState(pos);
        return state.isOf(this) && isFertilizable(world, pos, state);
    }

    protected int getPlantSize(WorldView world, BlockPos pos) {
        int plantSize = 1;
        while (world.getBlockState(pos.down(plantSize)).isOf(this)) {
            ++plantSize;
        }
        return plantSize;
    }

    protected boolean canGrowUpwards(WorldView world, BlockPos pos, BlockState state) {
        return world.isAir(pos.up())
                && getPlantSize(world, pos) < getMaxHeight()
                && state.get(getAgeProperty()) > Math.min(MAX_AGE_WHILE_COVERED, getMaxAge(state) - 1);
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
