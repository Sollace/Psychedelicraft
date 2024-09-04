/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.particle.FluidParticleEffect;
import ivorius.psychedelicraft.particle.PSParticles;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class GlassTubeBlock extends BlockWithEntity implements PipeInsertable {
    public static final MapCodec<GlassTubeBlock> CODEC = createCodec(GlassTubeBlock::new);

    public static final EnumProperty<IODirection> IN = EnumProperty.of("in", IODirection.class);
    public static final EnumProperty<IODirection> OUT = EnumProperty.of("out", IODirection.class);

    private static final double RADIUS = 0.05;
    private static final VoxelShape DEFAULT_SHAPE = VoxelShapes.cuboid(0.4, 0.4, 0.4, 0.6, 0.6, 0.6);
    private static final Function<Direction, VoxelShape> SHAPE_PART_CACHE = Util.memoize(direction -> {
        return VoxelShapes.cuboid(
                0.5 + Math.min(-RADIUS, direction.getOffsetX() * 0.5),
                0.5 + Math.min(-RADIUS, direction.getOffsetY() * 0.5),
                0.5 + Math.min(-RADIUS, direction.getOffsetZ() * 0.5),
                0.5 + Math.max(RADIUS, direction.getOffsetX() * 0.5),
                0.5 + Math.max(RADIUS, direction.getOffsetY() * 0.5),
                0.5 + Math.max(RADIUS, direction.getOffsetZ() * 0.5)
            );
    });
    private static final Function<BlockState, VoxelShape> SHAPE_CACHE = Util.memoize(state -> {
        return Stream.of(state.get(IN), state.get(OUT))
            .map(IODirection::getDirection).flatMap(Optional::stream)
            .map(SHAPE_PART_CACHE).reduce(VoxelShapes::union)
            .orElse(DEFAULT_SHAPE);
    });

    static EnumProperty<IODirection> getInverseProperty(EnumProperty<IODirection> property) {
        return property == IN ? OUT : IN;
    }

    protected GlassTubeBlock(Settings settings) {
        super(settings);
        setDefaultState(stateManager.getDefaultState()
                .with(IN, IODirection.NONE)
                .with(OUT, IODirection.NONE)
        );
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(IN, OUT);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE_CACHE.apply(state);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        IODirection placedDir = IODirection.LOOKUP.get(ctx.getSide());
        BlockState state = super.getPlacementState(ctx);

        World world = ctx.getWorld();
        BlockPos neighborPos = ctx.getBlockPos().offset(ctx.getSide().getOpposite());
        BlockState neighborState = world.getBlockState(neighborPos);

        return getConnectionsForRedirection(world, neighborPos, neighborState)
                .findFirst()
                .map(placingConnection -> {
                    IODirection other = getValidConnectionDirections(ctx.getWorld(), ctx.getBlockPos(), placingConnection, placedDir.getOpposite())
                            .findFirst()
                            .orElse(IODirection.NONE);

                    return state
                            .with(getInverseProperty(placingConnection), placedDir.getOpposite())
                            .with(placingConnection, other);
                }).orElseGet(() -> {
                    // try to place output against neighbour
                    IODirection in = getValidConnectionDirections(ctx.getWorld(), ctx.getBlockPos(), OUT, placedDir).findFirst().orElse(placedDir.getOpposite());
                    IODirection out = getValidConnectionDirections(ctx.getWorld(), ctx.getBlockPos(), IN, in).findFirst().orElse(in.getOpposite());
                    setDirection(state, in, out);
                    return Blocks.STONE.getDefaultState();
                });
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return setDirection(state,
                getConnectionStateForNeighborUpdate(pos, state, direction, neighborPos, neighborState, world, IN),
                getConnectionStateForNeighborUpdate(pos, state, direction, neighborPos, neighborState, world, OUT)
        );
    }

    private IODirection getConnectionStateForNeighborUpdate(BlockPos pos, BlockState state, Direction direction, BlockPos neighborPos, BlockState neighbor, WorldAccess world, EnumProperty<IODirection> property) {
        IODirection current = state.get(property);

        if (PipeInsertable.canConnectWith(world, state, pos, neighbor, neighborPos, direction, property == IN)) {
            if (current == IODirection.NONE || !PipeInsertable.canConnectWith(world, state, pos,
                    world.getBlockState(pos.offset(current.direction)),
                    pos.offset(current.direction), current.direction, property == IN)
                ) {
                return IODirection.LOOKUP.get(direction);
            }
        } else if (current.direction == direction) {
            return IODirection.NONE;
        }
        return current;
    }

    @Override
    public boolean acceptsConnectionFrom(WorldAccess world, BlockState state, BlockPos pos, BlockState neighborState, BlockPos neighborPos, Direction direction, boolean input) {
        return neighborState.isOf(this) && (state.get(input ? OUT : IN).direction == direction);
    }

    @Override
    public int tryInsert(ServerWorld world, BlockState state, BlockPos pos, Direction direction, ItemFluids fluids) {
        if (state.get(IN).direction == direction.getOpposite()) {
            return world.getBlockEntity(pos, PSBlockEntities.GLASS_TUBE).map(data -> {
                return data.tank.deposit(fluids);
            }).orElse(0);
        }

        return SPILL_STATUS;
    }

    public BlockState setDirection(BlockState state, IODirection in, IODirection out) {
        if (in != IODirection.NONE || out != IODirection.NONE) {
            in = in == IODirection.NONE ? out.getOpposite() : in;
            out = out == IODirection.NONE || in == out ? in.getOpposite() : out;
        }
        return state.with(IN, in).with(OUT, out);
    }

    private static Stream<IODirection> getValidConnectionDirections(WorldAccess world, BlockPos pos, EnumProperty<IODirection> property, IODirection exclude) {
        return List.of(IODirection.NORTH, IODirection.SOUTH, IODirection.EAST, IODirection.WEST, IODirection.DOWN, IODirection.UP).stream().filter(direction -> {
            if (direction == exclude) {
                return false;
            }
            BlockPos neighborPos = pos.offset(direction.direction);
            BlockState neighborState = world.getBlockState(neighborPos);
            IODirection neighborComplimentaryDirection = neighborState.getOrEmpty(getInverseProperty(property)).orElse(null);
            if (neighborComplimentaryDirection == null && !PipeInsertable.canConnectWith(world, neighborState, pos, neighborState, neighborPos, direction.direction, property == OUT)) {
                return false;
            }
            return getConnectionsForRedirection(world, neighborPos, neighborState).anyMatch(openConnection -> openConnection == getInverseProperty(property));
        });
    }

    private static Stream<EnumProperty<IODirection>> getConnectionsForRedirection(WorldAccess world, BlockPos pos, BlockState state) {
        return Stream.of(IN, OUT).filter(property -> {
            IODirection currentDirection = state.getOrEmpty(property).orElse(IODirection.NONE);
            if (currentDirection == IODirection.NONE) {
                return true;
            }
            BlockPos neighborPos = pos.offset(currentDirection.direction);
            BlockState neighborState = world.getBlockState(neighborPos);
            IODirection neighborComplimentaryDirection = neighborState.getOrEmpty(getInverseProperty(property)).orElse(null);
            if (neighborComplimentaryDirection == null && !PipeInsertable.canConnectWith(world, state, pos, neighborState, neighborPos, currentDirection.direction, property == OUT)) {
                return true;
            }
            return neighborComplimentaryDirection != null
                && neighborComplimentaryDirection != currentDirection.getOpposite();
        });
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        world.getBlockEntity(pos, PSBlockEntities.GLASS_TUBE).ifPresent(data -> {
            if (data.tank.getContents().amount() > 0 && state.get(OUT)
                    .getDirection()
                    .map(direction -> PipeInsertable.tryInsert(world, pos.offset(direction), direction, data.tank)).orElse(SPILL_STATUS) == SPILL_STATUS) {
                var fluid = data.tank.getContents().fluid();
                Direction outDirection = state.get(OUT).direction;
                Vector3f outVec = outDirection == null ? new Vector3f() : outDirection.getUnitVector();
                world.spawnParticles(
                        fluid.getPhysical().isOf(Fluids.WATER) ? ParticleTypes.DRIPPING_WATER
                            : fluid.getPhysical().isOf(Fluids.LAVA) ? ParticleTypes.DRIPPING_LAVA
                            : new FluidParticleEffect(PSParticles.DRIPPING_FLUID, fluid),
                        pos.getX() + 0.5 + outVec.x * 0.5,
                        pos.getY() + 0.5 + outVec.y * 0.5 - 0.2,
                        pos.getZ() + 0.5 + outVec.z * 0.5, 1, 0, 0, 0, 0);
                data.tank.drain(3);
            }
            if (data.tank.getContents().amount() > 0) {
                world.scheduleBlockTick(pos, this, 3);
            }
        });
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new Data(pos, state);
    }

    enum IODirection implements StringIdentifiable {
        NONE(null),
        UP(Direction.UP),
        DOWN(Direction.DOWN),
        NORTH(Direction.NORTH),
        SOUTH(Direction.SOUTH),
        EAST(Direction.EAST),
        WEST(Direction.WEST);

        static final Map<Direction, IODirection> LOOKUP = Arrays.stream(values())
                .filter(i -> i.getDirection().isPresent())
                .collect(Collectors.toMap(i -> i.getDirection().get(), Function.identity()));
        static final List<IODirection> VALUES = LOOKUP.values().stream().toList();

        private final Direction direction;
        private final String name = name().toLowerCase(Locale.ROOT);

        IODirection(@Nullable Direction direction) {
            this.direction = direction;
        }

        public IODirection getOpposite() {
            return direction == null ? this : LOOKUP.getOrDefault(direction.getOpposite(), NONE);
        }

        public Optional<Direction> getDirection() {
            return Optional.ofNullable(direction);
        }

        @Override
        public String asString() {
            return name;
        }
    }

    public static class Data extends BlockEntity implements Resovoir.ChangeListener {
        private final Resovoir tank = new Resovoir(30, this);

        public Data(BlockPos pos, BlockState state) {
            super(PSBlockEntities.GLASS_TUBE, pos, state);
        }

        @Override
        public void onLevelChange(Resovoir tank, int change) {
            if (tank.getAmount() > 0 && this.getWorld() instanceof ServerWorld sw) {
                sw.scheduleBlockTick(getPos(), getCachedState().getBlock(), 10);
            }
            markDirty();
        }

        @Override
        protected void writeNbt(NbtCompound nbt, WrapperLookup lookup) {
            nbt.put("tank", tank.toNbt(lookup));
        }

        @Override
        protected void readNbt(NbtCompound nbt, WrapperLookup lookup) {
            tank.fromNbt(nbt.getCompound("tank"), lookup);
        }
    }
}
