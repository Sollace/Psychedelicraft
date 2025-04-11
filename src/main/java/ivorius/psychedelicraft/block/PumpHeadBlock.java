package ivorius.psychedelicraft.block;

import ivorius.psychedelicraft.client.render.blocks.VoxelShapeUtil;
import ivorius.psychedelicraft.item.PSItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class PumpHeadBlock extends FacingBlock {
    private static final VoxelShape UP_SHAPE = VoxelShapes.union(
            createCuboidShape(3, 0, 3, 13, 13, 13),
            createCuboidShape(2, 12, 2, 14, 14, 14)
    );
    private static final VoxelShape NORTH_SHAPE = VoxelShapes.union(
            createCuboidShape(3, 3, 3, 13, 13, 16),
            createCuboidShape(2, 2, 2, 14, 14, 4)
    );
    private static final VoxelShape SOUTH_SHAPE = VoxelShapeUtil.rotate(NORTH_SHAPE, Direction.NORTH);

    public PumpHeadBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            default -> UP_SHAPE;
        };
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        return PSItems.PUMP.getDefaultStack();
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
    public boolean hasSidedTransparency(BlockState state) {
        return true;
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient && player.getAbilities().creativeMode) {
            checkSupport(state, world, pos);
        }

        super.onBreak(world, pos, state, player);
    }

    @Deprecated
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            super.onStateReplaced(state, world, pos, newState, moved);
            checkSupport(state, world, pos);
        }
    }

    @Deprecated
    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return direction.getOpposite() == state.get(FACING) && !state.canPlaceAt(world, pos)
            ? Blocks.AIR.getDefaultState()
            : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return isAttached(state, world.getBlockState(pos.offset(state.get(FACING).getOpposite())));
    }

    private void checkSupport(BlockState state, World world, BlockPos pos) {
        BlockPos bodyPos = pos.offset(state.get(FACING).getOpposite());
        if (isAttached(state, world.getBlockState(bodyPos))) {
            world.breakBlock(bodyPos, true);
        }
    }

    private boolean isAttached(BlockState headState, BlockState pistonState) {
        return pistonState.isOf(PSBlocks.PUMP) && pistonState.get(PumpBlock.POWERED) && getHeadFacing(pistonState.get(FACING)) == headState.get(FACING);
    }

    static Direction getHeadFacing(Direction facing) {
        return switch (facing) {
            case UP -> Direction.SOUTH;
            case DOWN -> Direction.NORTH;
            default -> Direction.UP;
        };
    }
}
