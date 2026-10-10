package ivorius.psychedelicraft.block;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.MoreObjects;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class PlanterBlock extends Block {
    public static final EnumProperty<Segment> SEGMENT = EnumProperty.of("segment", Segment.class);
    public static final EnumProperty<Direction.Axis> AXIS = Properties.HORIZONTAL_AXIS;
    public static final Direction.Axis DEFAULT_AXIS = Axis.X;

    private static final int LIP_WIDTH = 2;
    public static final int BODY_HEIGHT = 15;

    public static final VoxelShape[] LIP_SHAPES = Util.make(() -> {
        VoxelShape west = createCuboidShape(0, BODY_HEIGHT, 0, LIP_WIDTH, 16, 16);
        VoxelShape east = createCuboidShape(16 - LIP_WIDTH, BODY_HEIGHT, 0, 16, 16, 16);
        VoxelShape north = createCuboidShape(0, BODY_HEIGHT, 0, 16, 16, LIP_WIDTH);
        VoxelShape south = createCuboidShape(0, BODY_HEIGHT, 16 - LIP_WIDTH, 16, 16, 16);
        return createPlanterShapes(VoxelShapes.union(west, east, north, south),
                axis -> VoxelShapes.union(north, west, axis == 0 ? south : east),
                axis -> VoxelShapes.union(axis == 0 ? north : east, axis == 0 ? south : west),
                axis -> VoxelShapes.union(axis == 0 ? north : west, east, south),
                null
        );
    });
    private static final VoxelShape[] SHAPES = createPlanterShapes(VoxelShapes.union(
                createCuboidShape(1, 0, 1, 2, BODY_HEIGHT, 15),
                createCuboidShape(14, 0, 1, 15, BODY_HEIGHT, 15),
                createCuboidShape(1, 0, 1, 15, BODY_HEIGHT, 2),
                createCuboidShape(1, 0, 14, 15, BODY_HEIGHT, 15)
            ),
            axis -> VoxelShapes.union(
                    createCuboidShape(1, 0, 1, 16 - axis, BODY_HEIGHT, 2),
                    createCuboidShape(1, 0, 1, 2, BODY_HEIGHT, 15 + axis),
                    createCuboidShape(axis == 0 ? 1 : 14, 0, axis == 0 ? 14 : 1, 16 - axis, BODY_HEIGHT, 15 + axis)),
            axis -> VoxelShapes.union(
                    createCuboidShape(axis == 0 ? 0 : 14, 0, 1 - axis, 16 - axis, BODY_HEIGHT, axis == 0 ? 2 : 16),
                    createCuboidShape(axis, 0, axis == 0 ? 14 : 0, axis == 0 ? 16 : 2, BODY_HEIGHT, 15 + axis)),
            axis -> VoxelShapes.union(
                    createCuboidShape(axis, 0, 1 - axis, axis == 0 ? 15 : 2, BODY_HEIGHT, axis == 0 ? 2 : 15),
                    createCuboidShape(14, 0, 1 - axis, 15, BODY_HEIGHT, 15),
                    createCuboidShape(axis, 0, 14, 15, BODY_HEIGHT, 15)),
            (i, walls) -> VoxelShapes.union(LIP_SHAPES[i], createBase(i, 0, 1), walls));

    public static VoxelShape[] createPlanterShapes(VoxelShape all,
            Int2ObjectFunction<VoxelShape> start,
            Int2ObjectFunction<VoxelShape> middle,
            Int2ObjectFunction<VoxelShape> end,
            @Nullable BiFunction<Integer, VoxelShape, VoxelShape> remap) {
        var parts = List.of(i -> all, start, middle, end);
        return createShapeArray(8, i -> {
            VoxelShape shape = parts.get(i % 4).apply(i / 4);
            return remap == null ? shape : remap.apply(i, shape);
        });
    }

    public static VoxelShape createBase(int i, int minY, int maxY) {
        int axis = i / 4;
        int rotation = i % 4;
        int secondRotation = rotation % 3;
        int x = axis == 0 ? 1 : 0;
        int z = axis == 1 ? 1 : 0;
        int xMin = rotation > 1 ? x : 0;
        int zMin = rotation > 1 ? z : 0;
        int xMax = secondRotation > 0 ? x : 0;
        int zMax = secondRotation > 0 ? z : 0;
        return createCuboidShape(1 - xMin, minY, 1 - zMin, 15 + xMax, maxY, 15 + zMax);
    }

    public static VoxelShape getOutlineShape(VoxelShape[] shapes, BlockState state) {
        return shapes[(getAxis(state) == Direction.Axis.Z ? 4 : 0) + state.get(SEGMENT).ordinal()];
    }

    public PlanterBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(SEGMENT, Segment.SINGLE).with(AXIS, DEFAULT_AXIS));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(SEGMENT, AXIS);
    }

    @Nullable
    public static Direction.Axis getAxis(BlockState state) {
        return state.get(SEGMENT) == Segment.SINGLE ? null : state.get(AXIS);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return getOutlineShape(SHAPES, state);
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (stack.isOf(Items.DIRT)) {
            stack.decrementUnlessCreative(1, player);
            if (player instanceof ServerPlayerEntity spe) {
                spe.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
            }
            world.playSound(player, pos, Blocks.DIRT.getDefaultState().getSoundGroup().getPlaceSound(), SoundCategory.PLAYERS);
            world.setBlockState(pos, PSBlocks.PLANTER_FARMLAND.getDefaultState().with(AXIS, state.get(AXIS)).with(SEGMENT, state.get(SEGMENT)));
            return ActionResult.SUCCESS;
        }

        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
    }

    public static void updatePlanter(Block self, BlockState state, World world, BlockPos pos, BiPredicate<BlockPos, BlockState> updater) {
        if (!updater.test(pos, state)) {
            return;
        }
        Set<BlockPos> positions = new HashSet<>(Set.of(pos));
        Stack<Entry> stack = new Stack<>();
        stack.push(new Entry(pos, state.get(SEGMENT)));
        while (!stack.isEmpty()) {
            Entry entry = stack.pop();
            state = world.getBlockState(entry.pos());
            if (!state.isOf(self)) {
                return;
            }
            Segment segment = state.get(SEGMENT);
            Direction.Axis axis = getAxis(state);
            if (segment.hasLeft() && entry.segment().hasRight()) {
                BlockPos next = entry.pos().offset(axis.getNegativeDirection());
                if (positions.add(next)) {
                    stack.push(new Entry(next, segment));
                    if (!updater.test(next, state)) {
                        return;
                    }
                }
            }

            if (segment.hasRight() && entry.segment().hasLeft()) {
                BlockPos next = entry.pos().offset(axis.getPositiveDirection());
                if (positions.add(next)) {
                    stack.push(new Entry(next, segment));
                    if (!updater.test(next, state)) {
                        return;
                    }
                }
            }
        }
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        return getUpdatedState(state, world, pos, direction, neighborPos, neighborState);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return world.getBlockState(pos.down()).isSideSolidFullSquare(world, pos, Direction.UP);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getPlacementState(getDefaultState(), ctx.getWorld(), ctx.getBlockPos());
    }

    public static BlockState getUpdatedState(BlockState state, WorldView world, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState) {
        if (direction == Direction.DOWN && !state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        }

        if (direction.getAxis().isVertical()) {
            return state;
        }

        Direction.Axis axis = MoreObjects.firstNonNull(getAxis(state), direction.getAxis());
        Segment segment = state.get(SEGMENT);
        boolean hasLeft = segment.hasLeft();
        boolean hasRight = segment.hasRight();
        if (neighborState.isOf(state.getBlock())) {
            boolean matchAxis = getAxis(neighborState) == axis;

            if (axis.getNegativeDirection() == direction) {
                hasLeft = matchAxis;
            }
            if (axis.getPositiveDirection() == direction) {
                hasRight = matchAxis;
            }

            segment = Segment.of(hasLeft, hasRight);
        } else {
            boolean matchAxis = neighborState.contains(AXIS) && neighborState.contains(SEGMENT) && getAxis(neighborState) == axis;

            if (axis.getNegativeDirection() == direction && !(matchAxis && neighborState.get(SEGMENT).hasRight() == hasLeft)) {
                hasLeft = false;
            }
            if (axis.getPositiveDirection() == direction && !(matchAxis && neighborState.get(SEGMENT).hasLeft() == hasRight)) {
                hasRight = false;
            }

            segment = Segment.of(hasLeft, hasRight);
        }
        return state.with(SEGMENT, segment).with(AXIS, segment == Segment.SINGLE ? DEFAULT_AXIS : axis);
    }

    public static BlockState getPlacementState(BlockState state, WorldView world, BlockPos pos) {
        Direction.Axis axis = getAxis(state);
        if (axis != null) {
            return state.with(SEGMENT, getSegmentForAxis(state.getBlock(), world, pos, axis));
        }
        for (var newAxis : AXIS.getValues()) {
            Segment segment = getSegmentForAxis(state.getBlock(), world, pos, newAxis);
            if (segment != Segment.SINGLE) {
                return state.with(SEGMENT, segment).with(AXIS, newAxis);
            }
        }
        return state.with(SEGMENT, Segment.SINGLE).with(AXIS, DEFAULT_AXIS);
    }

    public static Segment getSegmentForAxis(Block self, WorldView world, BlockPos pos, @NotNull Direction.Axis axis) {
        boolean hasNegative = canConnect(self, world.getBlockState(pos.offset(axis.getNegativeDirection())), axis);
        boolean hasPositive = canConnect(self, world.getBlockState(pos.offset(axis.getPositiveDirection())), axis);
        return Segment.of(hasNegative, hasPositive);
    }

    public static boolean canConnect(Block self, BlockState state, @NotNull Direction.Axis axis) {
        return state.isOf(self) && MoreObjects.firstNonNull(getAxis(state), axis) == axis;
    }

    record Entry(BlockPos pos, Segment segment) {}

    public enum Segment implements StringIdentifiable {
        SINGLE,
        LEFT,
        MIDDLE,
        RIGHT;

        private final String name = name().toLowerCase(Locale.ROOT);

        @Override
        public String asString() {
            return name;
        }

        public static Segment of(boolean left, boolean right) {
            return left && right ? MIDDLE : left ? RIGHT : right ? LEFT : SINGLE;
        }

        public boolean hasLeft() {
            return this == RIGHT || this == MIDDLE;
        }

        public boolean hasRight() {
            return this == LEFT || this == MIDDLE;
        }
    }
}
