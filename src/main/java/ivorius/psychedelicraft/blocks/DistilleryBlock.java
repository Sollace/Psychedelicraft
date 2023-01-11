/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.blocks;

import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.block.entity.*;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.*;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.*;

/**
 * Created by lukas on 25.10.14.
 */
public class DistilleryBlock extends BlockWithFluid<DistilleryBlockEntity> {
    private static final VoxelShape SHAPE = Block.createCuboidShape(4, 0, 4, 12, 14.4, 12);
    public static final DirectionProperty FACING = Properties.FACING;

    public DistilleryBlock(Settings settings) {
        super(settings.nonOpaque());
        setDefaultState(getDefaultState().with(FACING, Direction.UP));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, Direction.Type.HORIZONTAL.stream().filter(direction -> {
            return canConnectTo(ctx.getWorld().getBlockState(ctx.getBlockPos().offset(direction)), direction);
        }).findAny().orElse(Direction.UP));
    }

    public static boolean canConnectTo(BlockState state, Direction direction) {
        return state.isIn(PSTags.BARRELS)
            || state.isOf(PSBlocks.MASH_TUB)
            || (state.isOf(PSBlocks.DISTILLERY) && state.get(FACING) != direction.getOpposite());
    }

    @Deprecated
    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(FACING) == direction) {
            return state.with(FACING, Direction.Type.HORIZONTAL.stream().filter(d -> {
                return canConnectTo(world.getBlockState(pos.offset(d)), d);
            }).findAny().orElse(Direction.UP));
        }

        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }


    @Override
    protected BlockEntityType<DistilleryBlockEntity> getBlockEntityType() {
        return PSBlockEntities.DISTILLERY;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING);
    }
}
