package ivorius.psychedelicraft.block;

import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import ivorius.psychedelicraft.client.render.blocks.VoxelShapeUtil;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.mojang.serialization.MapCodec;

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
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.*;

/**
 * Created by lukas on 16.11.14.
 */
public class BottleRackBlock extends BlockWithEntity {
    public static final MapCodec<BottleRackBlock> CODEC = createCodec(BottleRackBlock::new);
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    private static final Map<Direction, VoxelShape> SHAPES = Arrays.stream(Direction.values())
            .filter(d -> d.getAxis() != Axis.Y)
            .collect(Collectors.toMap(
                    Function.identity(),
                    VoxelShapeUtil.rotator(VoxelShapes.union(
                            Block.createCuboidShape(0, 0, 3, 1, 16, 11),
                            Block.createCuboidShape(5, 0, 3, 6, 16, 11),
                            Block.createCuboidShape(10, 0, 3, 11, 16, 11),
                            Block.createCuboidShape(15, 0, 3, 16, 16, 11),

                            Block.createCuboidShape(1, 0, 2.75F, 15, 1, 12.35F),
                            Block.createCuboidShape(1, 5, 2.75F, 15, 6, 12.35F),
                            Block.createCuboidShape(1, 10, 2.75F, 15, 11, 12.35F),
                            Block.createCuboidShape(1, 15, 2.75F, 15, 16, 12.35F)
                    )))
            );

    public BottleRackBlock(Settings settings) {
        super(settings.nonOpaque());
    }

    @Override
    protected MapCodec<? extends BottleRackBlock> getCodec() {
        return CODEC;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    @Deprecated
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPES.getOrDefault(state.get(FACING), VoxelShapes.fullCube());
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
    public ItemActionResult onUseWithItem(ItemStack heldStack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return world.getBlockEntity(pos, PSBlockEntities.BOTTLE_RACK).map(be -> {
            if (heldStack.isEmpty()) {
                TypedActionResult<ItemStack> extracted = be.extractItem(hit, state.get(FACING));
                if (extracted.getResult().isAccepted()) {
                    player.setStackInHand(hand, extracted.getValue());
                }
                return extracted.getResult() == ActionResult.SUCCESS ? ItemActionResult.SUCCESS : ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }

            return be.insertItem(heldStack, hit, state.get(FACING));
        }).orElse(ItemActionResult.FAIL);
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
