package ivorius.psychedelicraft.block;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.block.GlassTubeBlock.IODirection;
import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.recipe.FluidMound;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.FluidDrainable;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;

public class PumpBlock extends FacingBlock implements PipeInsertable {
    public static final MapCodec<PumpBlock> CODEC = createCodec(PumpBlock::new);

    public static final BooleanProperty POWERED = Properties.POWERED;

    public PumpBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(FACING, Direction.NORTH).with(POWERED, false));
    }

    @Override
    protected MapCodec<? extends PumpBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);
        boolean powered = world.isReceivingRedstonePower(pos);
        if (powered != state.get(POWERED)) {
            activate(state, world, pos, powered);
        }
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!oldState.isOf(state.getBlock())) {
            boolean powered = world.isReceivingRedstonePower(pos);
            if (powered) {
                activate(state, world, pos, powered);
            }
        }
    }

    private void activate(BlockState state, World world, BlockPos pos, boolean powered) {
        Direction facing = state.get(FACING);
        Direction headFacing = PumpHeadBlock.getHeadFacing(facing);
        BlockPos headPos = pos.offset(headFacing);
        BlockState headState = world.getBlockState(headPos);

        if (headState.isSolidBlock(world, headPos) && !headState.isOf(PSBlocks.PUMP_HEAD)) {
            return;
        }
        world.setBlockState(pos, state.with(POWERED, powered));

        if (powered) {
            if (!headState.isOf(PSBlocks.PUMP_HEAD)) {
                world.breakBlock(headPos, true);
                BlockState newHeadState = pushEntitiesUpBeforeBlockChange(headState, PSBlocks.PUMP_HEAD.getDefaultState().with(FACING, headFacing), world, headPos);
                world.setBlockState(headPos, newHeadState, Block.NOTIFY_LISTENERS);
            }
        } else {
            if (headState.isOf(PSBlocks.PUMP_HEAD)) {
                world.setBlockState(headPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
            }
        }

        world.playSound(null, pos, powered ? SoundEvents.BLOCK_PISTON_EXTEND : SoundEvents.BLOCK_PISTON_CONTRACT, SoundCategory.BLOCKS, 0.5F, world.random.nextFloat() * 0.25F + 0.6F);
        world.emitGameEvent(powered ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, pos, GameEvent.Emitter.of(state));
        if (!powered) {
            return;
        }

        if (!(world instanceof ServerWorld sw)) {
            return;
        }

        @Nullable
        BlockPos inPos = findInputPos(world, pos.offset(facing));
        if (inPos == null) {
            return;
        }

        BlockState inFluid = world.getBlockState(inPos);
        if (inFluid.getFluidState().isEmpty()) {
            return;
        }

        BlockPos outPos = pos.offset(facing.getOpposite());
        BlockState outState = world.getBlockState(outPos);
        outState.getOrEmpty(GlassTubeBlock.IN).orElse(IODirection.NONE).getDirection().ifPresent(inDirection -> {
            if (inDirection == facing) {
                PipeInsertable.tryInsert(sw, outPos, facing.getOpposite(), new PipeFluids(new FluidMound(List.of(
                        SimpleFluid.of(inFluid.getFluidState().getFluid()).getDefaultStack(FluidVolumes.BUCKET)
                )), 0));
                if (inFluid.getBlock() instanceof FluidDrainable drainable) {
                    drainable.tryDrainFluid(null, world, inPos, inFluid);
                }
            }
        });
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return super.getPlacementState(ctx)
                .with(FACING, ctx.getPlayerLookDirection().getOpposite());
    }

    @Override
    public boolean acceptsConnectionFrom(WorldAccess world, BlockState state, BlockPos pos, BlockState neighborState, BlockPos neighborPos, Direction direction, boolean input) {
        return !input && direction == state.get(FACING) && neighborState.getBlock() instanceof GlassTubeBlock;
    }

    @Nullable
    static BlockPos findInputPos(WorldAccess world, BlockPos initial) {
        return findInputPos(world, initial.mutableCopy(), new HashSet<>(), 10);
    }

    @Nullable
    static BlockPos findInputPos(WorldAccess world, BlockPos.Mutable mutable, Set<BlockPos> visited, int maxDepth) {
        BlockPos pos = mutable.toImmutable();
        if (!visited.add(pos)) {
            return null;
        }
        FluidState state = world.getFluidState(pos);

        if (state.isEmpty()) {
            return null;
        }

        if (state.isStill()) {
            return pos;
        }

        @Nullable
        BlockPos nearest = null;
        SimpleFluid main = SimpleFluid.of(state.getFluid());
        for (Direction dir : Direction.Type.HORIZONTAL) {
            FluidState neighbor = world.getFluidState(mutable.set(pos).move(dir));
            SimpleFluid fluid = SimpleFluid.of(neighbor.getFluid());
            if (fluid == main) {
                BlockPos p = findInputPos(world, mutable, visited, maxDepth - 1);
                if (p != null && (nearest == null || pos.getSquaredDistance(nearest) > pos.getSquaredDistance(p))) {
                    nearest = p;
                }
            }
        }

        if (nearest == null) {
            return findInputPos(world, mutable.move(Direction.DOWN), visited, maxDepth - 1);
        }

        return nearest;
    }
}
