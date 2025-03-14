/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import ivorius.psychedelicraft.block.entity.TrayBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class TrayBlock extends BlockWithEntity implements PipeInsertable {
    public static final MapCodec<TrayBlock> CODEC = createCodec(TrayBlock::new);

    private static final VoxelShape X_SHAPE = ShapeUtil.createCenteredShape(9, 2, 6);
    private static final VoxelShape Z_SHAPE = ShapeUtil.createCenteredShape(6, 2, 9);

    private static final EnumProperty<Direction.Axis> AXIS = Properties.HORIZONTAL_AXIS;

    public TrayBlock(Settings settings) {
        super(settings.nonOpaque());
        this.setDefaultState(getDefaultState().with(AXIS, Direction.Axis.X));
    }

    @Override
    protected MapCodec<? extends TrayBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(AXIS) == Axis.X ? X_SHAPE : Z_SHAPE;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(AXIS, ctx.getHorizontalPlayerFacing().rotateYClockwise().getAxis());
    }

    @Override
    public boolean acceptsConnectionFrom(WorldAccess world, BlockState state, BlockPos pos, BlockState neighborState, BlockPos neighborPos, Direction direction, boolean input) {
        return input && direction == Direction.UP;
    }

    @Override
    public Either<Optional<PipeFluids>, Unit> tryInsert(ServerWorld world, BlockState state, BlockPos pos, Direction direction, PipeFluids fluids) {
        if (direction != Direction.DOWN) {
            return PipeInsertable.reject(fluids);
        }
        return world.getBlockEntity(pos, PSBlockEntities.TRAY)
                .map(data -> data.tryInsert(world, state, pos, direction, fluids))
                .orElseGet(() -> PipeInsertable.reject(fluids));
    }

    @Override
    protected List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
        if (builder.getOptional(LootContextParameters.BLOCK_ENTITY) instanceof TrayBlockEntity be) {
            builder = builder.addDynamicDrop(BlockWithFluid.CONTENTS_DYNAMIC_DROP_ID, lootConsumer -> {
                be.getCraftingResult().ifPresent(result -> {
                    lootConsumer.accept(result);
                });
            });
        }
        return super.getDroppedStacks(state, builder);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TrayBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <Q extends BlockEntity> BlockEntityTicker<Q> getTicker(World world, BlockState state, BlockEntityType<Q> type) {
        return world.isClient
                ? null
                : validateTicker(type, PSBlockEntities.TRAY, (w, p, s, entity) -> entity.tick((ServerWorld)w));
    }
}
