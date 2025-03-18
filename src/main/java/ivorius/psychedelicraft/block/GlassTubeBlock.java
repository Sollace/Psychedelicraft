/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import ivorius.psychedelicraft.block.entity.SyncedBlockEntity;
import ivorius.psychedelicraft.particle.FluidParticleEffect;
import ivorius.psychedelicraft.particle.PSParticles;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class GlassTubeBlock extends BlockWithEntity {
    public static final MapCodec<GlassTubeBlock> CODEC = createCodec(GlassTubeBlock::new);

    public static final EnumProperty<IODirection> IN = EnumProperty.of("in", IODirection.class);
    public static final EnumProperty<IODirection> OUT = EnumProperty.of("out", IODirection.class);

    public static final double RADIUS = 0.06;
    private static final VoxelShape DEFAULT_SHAPE = VoxelShapes.cuboid(0.4, 0.4, 0.4, 0.6, 0.6, 0.6);
    private static final Function<Direction, VoxelShape> SHAPE_PART_CACHE = createShapePartCache(RADIUS, 0, 1);

    public static Function<Direction, VoxelShape> createShapePartCache(double radius, double offset, double length) {
        return Util.memoize(direction -> {
            return VoxelShapes.cuboid(
                    direction.getOffsetX() * offset + 0.5 + Math.min(-radius, direction.getOffsetX() * 0.5 * length),
                    direction.getOffsetY() * offset + 0.5 + Math.min(-radius, direction.getOffsetY() * 0.5 * length),
                    direction.getOffsetZ() * offset + 0.5 + Math.min(-radius, direction.getOffsetZ() * 0.5 * length),
                    direction.getOffsetX() * offset + 0.5 + Math.max(radius, direction.getOffsetX() * 0.5 * length),
                    direction.getOffsetY() * offset + 0.5 + Math.max(radius, direction.getOffsetY() * 0.5 * length),
                    direction.getOffsetZ() * offset + 0.5 + Math.max(radius, direction.getOffsetZ() * 0.5 * length)
                );
        });
    }

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
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {

        if (stack.isOf(Items.STICK)) {
            world.setBlockState(pos, state.with(IN, state.get(OUT)).with(OUT, state.get(IN)), Block.FORCE_STATE);
            world.playSound(player, pos, state.getSoundGroup().getPlaceSound(), SoundCategory.PLAYERS);
            return ItemActionResult.SUCCESS;
        }

        return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
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
                            .orElse(placedDir);

                    return state
                            .with(getInverseProperty(placingConnection), placedDir.getOpposite())
                            .with(placingConnection, other);
                }).orElseGet(() -> {
                    // try to place output against neighbour
                    IODirection in = getValidConnectionDirections(ctx.getWorld(), ctx.getBlockPos(), OUT, placedDir).findFirst().orElse(placedDir.getOpposite());
                    IODirection out = getValidConnectionDirections(ctx.getWorld(), ctx.getBlockPos(), IN, in).findFirst().orElse(in.getOpposite());
                    return setDirection(state, in, out);
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

    protected boolean isBlocked(BlockState state) {
        return false;
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
            if (neighborComplimentaryDirection == null
                    && !PipeInsertable.canConnectWith(world, neighborState, pos, neighborState, neighborPos, direction.direction, property == IN)) {
                return false;
            }
            return getConnectionsForRedirection(world, neighborPos, neighborState).anyMatch(openConnection -> openConnection == getInverseProperty(property));
        });
    }

    private static Stream<EnumProperty<IODirection>> getConnectionsForRedirection(WorldAccess world, BlockPos pos, BlockState state) {
        return Stream.of(IN, OUT).filter(property -> {
            IODirection currentDirection = state.getOrEmpty(property).orElse(IODirection.NONE);
            if (currentDirection == IODirection.NONE) {
                return false;
            }
            BlockPos neighborPos = pos.offset(currentDirection.direction);
            BlockState neighborState = world.getBlockState(neighborPos);
            IODirection neighborComplimentaryDirection = neighborState.getOrEmpty(getInverseProperty(property)).orElse(null);
            if (neighborComplimentaryDirection == null
                    && !PipeInsertable.canConnectWith(world, state, pos, neighborState, neighborPos, currentDirection.direction, property == IN)) {
                return true;
            }
            return neighborComplimentaryDirection != null
                && neighborComplimentaryDirection != currentDirection.getOpposite();
        });
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        world.getBlockEntity(pos, PSBlockEntities.GLASS_TUBE).ifPresent(data -> {
            data.pushContentsForward(world, pos, state.get(OUT));
        });
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);
        if (world instanceof ServerWorld sw) {
            world.getBlockEntity(pos, PSBlockEntities.GLASS_TUBE).ifPresent(data -> {
                data.pushContentsForward(sw, pos, state.get(OUT));
                data.pullContentsForward(sw, pos);
            });
        }
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return world.getBlockEntity(pos, PSBlockEntities.GLASS_TUBE).map(data -> (int)((data.contents.size() / 10F) * 15)).orElse(0);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new Data(pos, state);
    }

    public enum IODirection implements StringIdentifiable {
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

    static int getTemperatureDropPerTransfer(ServerWorld world, BlockPos center) {
        return 1 + BlockPos.streamOutwards(center, 1, 1, 1).mapToInt(pos -> {
            return pos.equals(center) ? 0 : getTemperatureDropFor(world, pos);
        }).sum();
    }

    static int getTemperatureDropFor(ServerWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.isIn(BlockTags.ICE) || state.isIn(BlockTags.SNOW)) {
            return 1;
        }
        if (state.isIn(BlockTags.FIRE) || (state.isIn(BlockTags.CAMPFIRES) && state.getOrEmpty(Properties.LIT).orElse(false))) {
            return -1;
        }
        if (state.getFluidState().isIn(FluidTags.LAVA)) {
            return -4;
        }

        return 0;
    }

    public static class Data extends SyncedBlockEntity implements PipeInsertable {
        private final List<PipeFluids> contents = new LinkedList<>();

        private int temperatureUpdateCooldown;
        private int temperatureDrop;

        public Data(BlockPos pos, BlockState state) {
            super(PSBlockEntities.GLASS_TUBE, pos, state);
        }

        public List<PipeFluids> getContents() {
            return contents;
        }

        private int getTemperatureDrop(ServerWorld world, BlockPos pos) {
            if (temperatureUpdateCooldown == 0) {
                temperatureUpdateCooldown = 200;
                temperatureDrop = getTemperatureDropPerTransfer(world, pos);
            }
            return temperatureDrop;
        }

        public Either<Optional<PipeFluids>, Unit> receiveContents(ServerWorld world, BlockPos pos, BlockState state, PipeFluids contents) {
            pushContentsForward(world, pos, state.get(OUT));
            var status = Optional.of(contents).filter(i -> !i.isEmpty()).map(i -> i.withTemperature(i.temperature() - getTemperatureDrop(world, pos))).map(newContents -> {
                if (this.contents.size() < 10) {
                    this.contents.addFirst(newContents);
                } else {
                    PipeFluids first = this.contents.getFirst();
                    if (first.isEmpty()) {
                        this.contents.removeFirst();
                        this.contents.addFirst(contents);
                    } else {
                        Optional<PipeFluids> available = this.contents.stream().filter(i -> i.fluids().totalSize() < 100).findFirst();
                        if (available.isEmpty()) {
                            return PipeInsertable.reject(contents);
                        }
                        this.contents.set(this.contents.indexOf(available.get()), available.get().combine(newContents));
                    }
                }
                markDirty();

                return STATUS_ACCEPT_ALL;
            }).orElse(STATUS_ACCEPT_ALL);

            scheduleNextTick(world);
            return status;
        }

        public void pushContentsForward(ServerWorld world, BlockPos pos, IODirection direction) {
            if (((GlassTubeBlock)getCachedState().getBlock()).isBlocked(getCachedState())) {
                return;
            }

            markDirty();

            if (contents.size() >= 10) {
                PipeFluids fluids = contents.removeLast();
                if (!fluids.isEmpty()) {
                    world.playSound(null, pos, SoundEvents.BLOCK_BUBBLE_COLUMN_BUBBLE_POP, SoundCategory.BLOCKS, 0.1F, 0.001F);
                    Optional<PipeFluids> pushedBack = fluids.isEmpty() ? Optional.empty() : direction.getDirection().map(d -> PipeInsertable.tryInsert(world, pos.offset(d), d, fluids)).orElse(STATUS_VOIDED).ifRight(unit -> {
                        fluids.fluids().getFluids().forEach(fluid -> {
                            Vector3f outVec = direction.getDirection().map(Direction::getUnitVector).orElseGet(Vector3f::new);
                            world.spawnParticles(
                                    fluid.fluid().getPhysical().isOf(Fluids.WATER) ? ParticleTypes.DRIPPING_WATER
                                        : fluid.fluid().getPhysical().isOf(Fluids.LAVA) ? ParticleTypes.DRIPPING_LAVA
                                        : new FluidParticleEffect(PSParticles.DRIPPING_FLUID, fluid.fluid()),
                                    pos.getX() + 0.5 + outVec.x * 0.5,
                                    pos.getY() + 0.5 + outVec.y * 0.5 - 0.2,
                                    pos.getZ() + 0.5 + outVec.z * 0.5, 1, 0, 0, 0, 0);
                        });
                    }).left().flatMap(Function.identity());
                    if (pushedBack.isPresent() && !pushedBack.get().isEmpty()) {
                        contents.addFirst(pushedBack.get());
                    }
                }
            } else {
                contents.addFirst(PipeFluids.EMPTY);
            }

            scheduleNextTick(world);
        }

        private void scheduleNextTick(ServerWorld world) {
            for (var i : contents) {
                if (!i.isEmpty()) {
                    world.scheduleBlockTick(pos, getCachedState().getBlock(), 3);
                    return;
                }
            }
        }

        public void pullContentsForward(ServerWorld world, BlockPos pos) {
            if (((GlassTubeBlock)getCachedState().getBlock()).isBlocked(getCachedState())) {
                return;
            }

            markDirty();

            if (world.getReceivedStrongRedstonePower(pos) == 15) {
                getCachedState().get(IN).getDirection().flatMap(d -> PipeInsertable.tryExtract(world, pos.offset(d), d)).ifPresent(contents::addFirst);
            }

            scheduleNextTick(world);
        }

        @Override
        public boolean acceptsConnectionFrom(WorldAccess world, BlockState state, BlockPos pos, BlockState neighborState, BlockPos neighborPos, Direction direction, boolean input) {
            return neighborState.getBlock() instanceof GlassTubeBlock && (state.get(input ? OUT : IN).direction == direction);
        }

        @Override
        public Either<Optional<PipeFluids>, Unit> tryInsert(ServerWorld world, BlockState state, BlockPos pos, Direction direction, PipeFluids fluids) {
            if (state.get(IN).direction == direction.getOpposite()) {
                return receiveContents(world, pos, state, fluids);
            }

            return PipeInsertable.reject(fluids);
        }

        @Override
        protected void writeNbt(NbtCompound nbt, WrapperLookup lookup) {
            super.writeNbt(nbt, lookup);
            PipeFluids.LIST_CODEC.encodeStart(NbtOps.INSTANCE, contents).result()
                .ifPresent(el -> nbt.put("contents", el));
        }

        @Override
        protected void readNbt(NbtCompound nbt, WrapperLookup lookup) {
            super.readNbt(nbt, lookup);
            contents.clear();
            PipeFluids.LIST_CODEC.decode(NbtOps.INSTANCE, nbt.get("contents")).result().map(Pair::getFirst).ifPresent(contents::addAll);
        }
    }
}
