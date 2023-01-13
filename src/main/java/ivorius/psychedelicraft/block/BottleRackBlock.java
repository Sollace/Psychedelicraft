package ivorius.psychedelicraft.block;

import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import ivorius.psychedelicraft.block.entity.BottleRackBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.*;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.*;

/**
 * Created by lukas on 16.11.14.
 */
public class BottleRackBlock extends BlockWithEntity {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    // TODO: rotations
    private static final VoxelShape[] SHAPES = new VoxelShape[] {
            Block.createCuboidShape(-8, -8, -1.6, 16, 16, 9.6),
            Block.createCuboidShape(-8, -8, -1.6, 16, 16, 9.6),
            Block.createCuboidShape(-8, -8, -1.6, 16, 16, 9.6),
            Block.createCuboidShape(-8, -8, -1.6, 16, 16, 9.6)
    };

    public BottleRackBlock(Settings settings) {
        super(settings.nonOpaque());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite());
    }

    @Override
    @Deprecated
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPES[state.get(FACING).getHorizontal()];
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
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return world.getBlockEntity(pos, PSBlockEntities.BOTTLE_RACK).map(be -> {

            ItemStack heldStack = player.getStackInHand(hand);

            if (heldStack.isEmpty()) {
                TypedActionResult<ItemStack> extracted = be.extractItem(hit, state.get(FACING));
                if (extracted.getResult().isAccepted()) {
                    player.setStackInHand(hand, extracted.getValue());
                }
                return extracted.getResult();
            }

            return be.insertItem(player.getStackInHand(hand), hit, state.get(FACING));
        }).orElse(ActionResult.FAIL);
    }

    @Deprecated
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock()) && !world.isClient) {
            world.getBlockEntity(pos, PSBlockEntities.BOTTLE_RACK).ifPresent(be -> {
                ItemScatterer.spawn(world, pos, be);
                world.updateComparators(pos, this);
            });
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BottleRackBlockEntity(pos, state);
    }
}
