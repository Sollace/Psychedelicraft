package ivorius.psychedelicraft.block;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.PointedDripstoneBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.ScheduledTickView;

public class PlanterFarmBlock extends FarmlandBlock {
    public static final EnumProperty<PlanterBlock.Segment> SEGMENT = PlanterBlock.SEGMENT;
    public static final EnumProperty<Direction.Axis> AXIS = Properties.HORIZONTAL_AXIS;
    public static final Direction.Axis DEFAULT_AXIS = Axis.X;
    private static final VoxelShape[] SHAPES = createShapeArray(PlanterBlock.LIP_SHAPES.length - 1, i -> VoxelShapes.union(PlanterBlock.LIP_SHAPES[i], PlanterBlock.createBase(i, 0, PlanterBlock.BODY_HEIGHT)));

    public PlanterFarmBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(SEGMENT, PlanterBlock.Segment.SINGLE).with(AXIS, DEFAULT_AXIS));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(SEGMENT, AXIS);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return PlanterBlock.getOutlineShape(SHAPES, state);
    }

    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        // Noop
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(MOISTURE) < MAX_MOISTURE) {
            @Nullable
            BlockPos dripPos = getDripPos(world, pos);
            if (dripPos != null) {
                Fluid fluid = PointedDripstoneBlock.getDripFluid(world, dripPos);
                if (fluid == Fluids.WATER) {
                    world.setBlockState(pos, state.cycle(MOISTURE), Block.NOTIFY_LISTENERS);
                    world.syncWorldEvent(WorldEvents.POINTED_DRIPSTONE_DRIPS_WATER_INTO_CAULDRON, pos, 0);
                }
            }
        }
    }

    @Override
    public void precipitationTick(BlockState state, World world, BlockPos pos, Biome.Precipitation precipitation) {
        if (state.get(MOISTURE) < MAX_MOISTURE) {
            world.setBlockState(pos, state.cycle(MOISTURE), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemFluids fluids = ItemFluids.of(stack);
        if (fluids.isOf(Fluids.WATER) && fluids.amount() >= FluidVolumes.BUCKET) {
            if (state.get(MOISTURE) >= MAX_MOISTURE) {
                return ActionResult.FAIL;
            }
            ItemFluids.Transaction t = ItemFluids.Transaction.begin(stack.copy());
            t.withdraw(FluidVolumes.BUCKET);
            player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, t.toItemStack(), false));
            player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
            world.setBlockState(pos, state.with(MOISTURE, MAX_MOISTURE));
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(state));
            world.playSound(player, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1, 1);
            world.emitGameEvent(player, GameEvent.FLUID_PLACE, pos);
            return ActionResult.SUCCESS;
        }
        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        int moisture = state.get(MOISTURE);
        if (moisture > 0 && !world.hasRain(pos.up())) {
            world.setBlockState(pos, state.with(MOISTURE, moisture - 1), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (direction == Direction.UP && !state.canPlaceAt(world, pos)) {
            tickView.scheduleBlockTick(pos, this, 1);
        }

        return PlanterBlock.getUpdatedState(state, world, pos, direction, neighborPos, neighborState);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return world.getBlockState(pos.down()).isSideSolidFullSquare(world, pos, Direction.UP);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return PlanterBlock.getPlacementState(getDefaultState(), ctx.getWorld(), ctx.getBlockPos());
    }

    @Override
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        return PSItems.PLANTER.getDefaultStack();
    }

    @Nullable
    static BlockPos getDripPos(World world, BlockPos pos) {
        BlockPos.Mutable mutable = pos.mutableCopy();
        do {
            mutable.move(Direction.UP);
        } while (world.getBlockState(mutable).isIn(PSTags.Blocks.WATER_PERMIATES_THROUGH));
        return PointedDripstoneBlock.getDripPos(world, mutable);
    }
}
