/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.blocks;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import ivorius.psychedelicraft.block.entity.RiftJarBlockEntity;
import ivorius.psychedelicraft.items.RiftJarItem;
import ivorius.psychedelicraft.items.PSItems;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

class RiftJarBlock extends BlockWithEntity {
    private static final VoxelShape SHAPE = Block.createCuboidShape(1.6, 1.6, 1.6, 14.4, 12.8, 14.4);
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    public RiftJarBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    @Deprecated
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
        return getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite());
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return world.getBlockEntity(pos, PSBlockEntities.RIFT_JAR).map(be -> {
            if (player.isSneaking()) {
                be.toggleSuckingRifts();
            } else {
                be.toggleRiftJarOpen();
            }
            return ActionResult.SUCCESS;
        }).orElse(ActionResult.FAIL);
    }

    @Deprecated
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock()) && !world.isClient) {
            world.getBlockEntity(pos, PSBlockEntities.RIFT_JAR).ifPresent(be -> {

                if (!be.jarBroken) {
                    Block.dropStack(world, pos, RiftJarItem.createFilledRiftJar(be.currentRiftFraction, PSItems.RIFT_JAR));
                }
            });
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RiftJarBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <Q extends BlockEntity> BlockEntityTicker<Q> getTicker(World world, BlockState state, BlockEntityType<Q> type) {
        return world.isClient
                ? checkType(type, PSBlockEntities.RIFT_JAR, (w, p, s, entity) -> entity.tickAnimation())
                : checkType(type, PSBlockEntities.RIFT_JAR, (w, p, s, entity) -> entity.tick((ServerWorld)w));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING);
    }
}
