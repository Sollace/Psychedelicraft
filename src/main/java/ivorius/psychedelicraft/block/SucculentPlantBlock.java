/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.*;

public abstract class SucculentPlantBlock extends PlantBlock implements Fertilizable {
    public SucculentPlantBlock(Settings settings) {
        super(settings.offsetType(OffsetType.XZ));
        setDefaultState(getDefaultState().with(getAgeProperty(), 0));
    }

    protected abstract IntProperty getAgeProperty();

    protected abstract int getMaxAge();

    protected abstract int getGrowthRate(BlockState state);

    protected abstract VoxelShape[] getShapes();

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(getAgeProperty());
    }

    @Override
    public final VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Vec3d offset = state.getModelOffset(world, pos);
        return getShapes()[state.get(getAgeProperty())].offset(offset.x, offset.y, offset.z);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return floor.isSideSolid(world, pos, Direction.UP, SideShapeType.CENTER);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (isFertilizable(world, pos, state, false)) {
            if (world.random.nextInt(getGrowthRate(state)) == 0) {
                applyGrowth(world, random, pos, state, false);
            }
        }
    }

    public void applyGrowth(World world, Random random, BlockPos pos, BlockState state, boolean bonemeal) {
        if (state.get(getAgeProperty()) < getMaxAge()) {
            world.setBlockState(pos, state.cycle(getAgeProperty()), Block.NOTIFY_ALL);
            return;
        }

        BlockPos.Mutable plantingPos = new BlockPos.Mutable();
        int i = 0;
        do {
            plantingPos.set(pos);
            plantingPos.add(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);

            if (world.isAir(plantingPos) && canPlaceAt(state, world, pos)) {
                world.setBlockState(plantingPos, getDefaultState(), Block.NOTIFY_ALL);
                return;
            }
        } while (++i < 4);
    }

    @Override
    public boolean isFertilizable(BlockView world, BlockPos pos, BlockState state, boolean client) {
        return state.get(getAgeProperty()) < getMaxAge();
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        applyGrowth(world, random, pos, state, true);
    }
}
