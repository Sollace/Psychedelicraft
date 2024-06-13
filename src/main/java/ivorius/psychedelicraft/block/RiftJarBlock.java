/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.PSSounds;
import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import ivorius.psychedelicraft.block.entity.RiftJarBlockEntity;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.RiftJarItem;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

class RiftJarBlock extends BlockWithEntity {
    public static final MapCodec<RiftJarBlock> CODEC = createCodec(RiftJarBlock::new);
    private static final VoxelShape SHAPE = VoxelShapes.union(
            Block.createCuboidShape(4, 0, 4, 12, 5, 12),
            Block.createCuboidShape(4.5, 5, 4.5, 11.5, 7, 11.5),
            Block.createCuboidShape(4, 7, 4, 12, 12, 12),
            Block.createCuboidShape(5, 12, 5, 11, 14, 11)
    );
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    public RiftJarBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends RiftJarBlock> getCodec() {
        return CODEC;
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
        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        return world.getBlockEntity(pos, PSBlockEntities.RIFT_JAR).map(be -> {
            if (player.isSneaking()) {
                be.toggleSuckingRifts();
                world.playSound(null, pos, PSSounds.BLOCK_RIFT_JAR_TOGGLE, SoundCategory.BLOCKS);
            } else {
                world.playSound(null, pos, be.toggleRiftJarOpen() ? PSSounds.BLOCK_RIFT_JAR_OPEN : PSSounds.BLOCK_RIFT_JAR_CLOSE, SoundCategory.BLOCKS);
            }
            return ActionResult.SUCCESS;
        }).orElse(ActionResult.FAIL);
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
        return Optional.ofNullable(builder.getOptional(LootContextParameters.BLOCK_ENTITY)).map(RiftJarBlockEntity.class::cast).map(be -> {
            if (!be.jarBroken) {
                return RiftJarItem.createFilledRiftJar(be.currentRiftFraction, PSItems.RIFT_JAR);
            }
            return null;
        }).stream().toList();
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RiftJarBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <Q extends BlockEntity> BlockEntityTicker<Q> getTicker(World world, BlockState state, BlockEntityType<Q> type) {
        return world.isClient
                ? validateTicker(type, PSBlockEntities.RIFT_JAR, (w, p, s, entity) -> entity.tickAnimation())
                : validateTicker(type, PSBlockEntities.RIFT_JAR, (w, p, s, entity) -> entity.tick((ServerWorld)w));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING);
    }
}
