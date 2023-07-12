/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.block.*;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class LatticeBlock extends HorizontalConnectingBlock implements Waterloggable {

    private static final Set<Direction> X_DIRECTIONS = Set.of(Direction.EAST, Direction.WEST);
    private static final Set<Direction> Z_DIRECTIONS = Set.of(Direction.NORTH, Direction.SOUTH);

    private static final Map<Direction.Axis, Set<Direction>> DEFAULT_CONNECTIONS = Map.of(
            Direction.Axis.X, X_DIRECTIONS,
            Direction.Axis.Z, Z_DIRECTIONS
    );

    public LatticeBlock(Settings settings) {
        super(1.6F, 1.6F, 16, 16, 16, settings);
        setDefaultState(getDefaultState().with(EAST, true).with(WEST, true).with(WATERLOGGED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, WATERLOGGED);
    }

    @Override
    @Deprecated
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED).booleanValue()) {
            world.createAndScheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        if (direction.getAxis().getType() == Direction.Type.HORIZONTAL) {
            Set<Direction> connections = getPossibleConnections(world, pos);

            if (connections.isEmpty()) {
                connections = (state.get(NORTH) || state.get(SOUTH)) ? Z_DIRECTIONS : X_DIRECTIONS;
            }

            return applyConnections(state, connections);
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        World world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();
        BlockState placedAgainst = world.getBlockState(pos.offset(ctx.getSide().getOpposite()));

        Set<Direction> connections;

        if (ctx.getSide().getAxis() == Direction.Axis.Y && placedAgainst.getBlock() instanceof LatticeBlock) {
            connections = getConnections(placedAgainst).collect(Collectors.toSet());
        } else {
            connections = getPossibleConnections(world, pos);
        }

        if (connections.isEmpty()) {
            connections = DEFAULT_CONNECTIONS.getOrDefault(ctx.getPlayerFacing().rotateYClockwise().getAxis(), Set.of());
        }

        return applyConnections(super.getPlacementState(ctx), connections)
                .with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER);
    }

    private Set<Direction> getPossibleConnections(WorldAccess world, BlockPos pos) {
        return FACING_PROPERTIES.keySet().stream().filter(connection -> canConnect(world, pos, connection)).collect(Collectors.toSet());
    }

    private boolean canConnect(WorldAccess world, BlockPos pos, Direction connectionDirection) {
        Direction neighbourDirection = connectionDirection.getOpposite();
        BlockPos neighbourPos = pos.offset(connectionDirection);
        BlockState neighbourState = world.getBlockState(neighbourPos);

        if (neighbourState.isAir()) {
            return false;
        }

        if (isLattice(neighbourState)) {
            return true;
        }

        BlockPos oppositePos = pos.offset(neighbourDirection);
        BlockState oppositeState = world.getBlockState(oppositePos);

        boolean neighbour = canConnectTo(world, neighbourPos, neighbourState, neighbourDirection);
        boolean opposite = canConnectTo(world, oppositePos, oppositeState, connectionDirection);

        return neighbour && opposite;
    }

    protected boolean canConnectTo(WorldAccess world, BlockPos pos, BlockState state, Direction direction) {
        return isLattice(state)
                || (!cannotConnect(state) && state.isSideSolidFullSquare(world, pos, direction))
                || (state.getBlock() instanceof FenceGateBlock && FenceGateBlock.canWallConnect(state, direction));
    }

    private boolean isLattice(BlockState state) {
        return state.getBlock() instanceof LatticeBlock;
    }

    @Deprecated
    @Override
    public FluidState getFluidState(BlockState state) {
        if (state.get(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getStill(false);
        }
        return super.getFluidState(state);
    }

    public static BlockState copyStateProperties(BlockState newState, BlockState state) {
        for (var connector : FACING_PROPERTIES.entrySet()) {
            newState = newState.with(connector.getValue(), state.get(connector.getValue()));
        }
        return newState.with(WATERLOGGED, state.get(WATERLOGGED));
    }

    public static BlockState applyConnections(BlockState state, Set<Direction> connections) {
        for (var connector : FACING_PROPERTIES.entrySet()) {
            state = state.with(connector.getValue(), connections.contains(connector.getKey()));
        }
        return state;
    }

    public static Stream<Direction> getConnections(BlockState state) {
        return FACING_PROPERTIES.entrySet().stream()
                .filter(connector -> state.get(connector.getValue()))
                .map(Map.Entry::getKey);
    }

    public static Stream<Direction> getFreeConnections(BlockState state) {
        return FACING_PROPERTIES.entrySet().stream()
                .filter(connector -> !state.get(connector.getValue()))
                .map(Map.Entry::getKey);
    }
}
