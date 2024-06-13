package ivorius.psychedelicraft.block;

import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import ivorius.psychedelicraft.block.entity.SyncedBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class PlacedDrinksBlock extends BlockWithEntity {
    public static final MapCodec<PlacedDrinksBlock> CODEC = createCodec(PlacedDrinksBlock::new);
    private static final Optional<TypedActionResult<ItemStack>> FAILURE = Optional.of(TypedActionResult.fail(ItemStack.EMPTY));
    private static final VoxelShape SHAPE = Block.createCuboidShape(0, 0, 0, 16, 1, 16);

    protected PlacedDrinksBlock(Settings settings) {
        super(settings.noCollision());
    }

    @Override
    protected MapCodec<? extends PlacedDrinksBlock> getCodec() {
        return CODEC;
    }

    @Override
    public boolean canMobSpawnInside(BlockState state) {
        return true;
    }

    @Override
    @Deprecated
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    @Deprecated
    public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        return SHAPE;
    }

    @Override
    public boolean hasSidedTransparency(BlockState state) {
        return true;
    }

    @Override
    public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        return 1;
    }

    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        return Psychedelicraft.getCrossHairTarget()
                .filter(hit -> hit.getType() == HitResult.Type.BLOCK)
                .map(hit -> (BlockHitResult)hit)
                .filter(hit -> hit.getBlockPos().equals(pos))
                .flatMap(Data::getHitPos)
                .flatMap(hitPos -> world.getBlockEntity(pos, PSBlockEntities.PLACED_DRINK).flatMap(be -> be.getDrink(hitPos)))
                .orElseGet(() -> super.getPickStack(world, pos, state));
    }

    public static boolean canPlace(ItemStack stack) {
        return stack.isIn(PSTags.Items.PLACEABLE);
    }

    @Override
    public ItemActionResult onUseWithItem(ItemStack heldStack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return world.getBlockEntity(pos, PSBlockEntities.PLACED_DRINK).flatMap(be -> {

            if (heldStack.isEmpty()) {
                return Data.getHitPos(hit).map(be::removeDrink).map(extracted -> {
                    ItemStack stack = extracted.getValue();
                    if (!stack.isEmpty()) {
                        player.giveItemStack(stack);
                    }
                    return extracted;
                });
            }

            if (!canPlace(heldStack)) {
                return FAILURE;
            }

            return Data.getHitPos(hit).map(position -> {
                return be.placeDrink(position, player.isCreative() ? heldStack.copyWithCount(1) : heldStack, player.getHeadYaw());
            });
        }).map(TypedActionResult::getResult).orElse(ActionResult.FAIL).isAccepted() ? ItemActionResult.SUCCESS : ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    @Deprecated
    public void onStacksDropped(BlockState state, ServerWorld world, BlockPos pos, ItemStack stack, boolean dropExperience) {
        world.getBlockEntity(pos, PSBlockEntities.PLACED_DRINK).ifPresent(be -> {
            be.forEachDrink((y, entry) -> {
                Block.dropStack(world, pos, entry.stack());
                return 0;
            });
            be.entries.clear();
        });
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isClient && (entity.getY() > pos.getY() || entity.getY() >= pos.getY() && !entity.isSneaking()) && entity instanceof LivingEntity && Math.max(
                Math.abs(entity.getX() - entity.lastRenderX),
                Math.abs(entity.getZ() - entity.lastRenderZ)
            ) >= 0.003F) {
            Block.dropStacks(state, world, pos, world.getBlockEntity(pos), entity, ItemStack.EMPTY);
            world.removeBlock(pos, false);
            world.playSound(null, pos, SoundEvents.BLOCK_CANDLE_PLACE, SoundCategory.BLOCKS);
        }
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new Data(pos, state);
    }

    public static ActionResult tryPlace(ItemUsageContext context) {
        if (!canPlace(context.getStack())) {
            return ActionResult.PASS;
        }

        BlockState state = context.getWorld().getBlockState(context.getBlockPos());
        boolean replaceable = state.canReplace(new ItemPlacementContext(context));

        if (!replaceable && context.getSide() != Direction.UP) {
            return ActionResult.PASS;
        }

        if (state.isOf(PSBlocks.PLACED_DRINK)) {
            return state.onUseWithItem(context.getStack(), context.getWorld(), context.getPlayer(), context.getHand(), new BlockHitResult(
                    context.getHitPos(), context.getSide(), context.getBlockPos(), true
            )).isAccepted() ? ActionResult.SUCCESS : ActionResult.PASS;
        }

        BlockPos blockPos = replaceable ? context.getBlockPos() : context.getBlockPos().offset(context.getSide());
        if (!replaceable && !context.getWorld().isAir(blockPos)) {
            return ActionResult.PASS;
        }
        BlockPos hitPos = Data.getHitPos(blockPos, context.getHitPos());
        context.getWorld().setBlockState(blockPos, PSBlocks.PLACED_DRINK.getDefaultState());
        return context.getWorld().getBlockEntity(blockPos, PSBlockEntities.PLACED_DRINK).map(be -> {
            return be.placeDrink(hitPos, context.getStack().split(1), context.getPlayerYaw()).getResult().isAccepted() ? ItemActionResult.SUCCESS : ItemActionResult.FAIL;
        }).orElse(ItemActionResult.FAIL).isAccepted() ? ActionResult.SUCCESS : ActionResult.FAIL;
    }

    public static class Data extends SyncedBlockEntity {
        static final int MAX_COORD = 16;
        static final int MAX_INDEX = MAX_COORD * MAX_COORD;
        static final int MAX_STACK_HEIGHT = 5;

        private final IntObjectMap<Stack<Entry>> entries = new IntObjectHashMap<>(MAX_INDEX);

        public Data(BlockPos pos, BlockState state) {
            super(PSBlockEntities.PLACED_DRINK, pos, state);
        }

        public void forEachDrink(DrinkConsumer consumer) {
            entries.values().forEach(list -> {
                final float[] y = new float[1];
                list.forEach(drink -> {
                    y[0] += consumer.accept(y[0], drink);
                });
            });
        }

        public TypedActionResult<ItemStack> removeDrink(BlockPos center) {
            return StreamSupport.stream(BlockPos.iterateInSquare(center, 2, Direction.EAST, Direction.NORTH).spliterator(), false).map(pos -> {
                int index = getIndex(pos);
                Stack<Entry> list = entries.get(index);
                if (list != null) {
                    Entry entry = list.pop();
                    if (entry != null) {
                        if (list.isEmpty()) {
                            entries.remove(index);
                            if (entries.isEmpty()) {
                                getWorld().removeBlock(getPos(), false);
                            }
                        }
                        markDirty();
                        return TypedActionResult.success(entry.stack());
                    }
                }

                return TypedActionResult.fail(ItemStack.EMPTY);
            }).filter(i -> i.getResult().isAccepted()).findFirst().orElseGet(() -> TypedActionResult.fail(ItemStack.EMPTY));
        }

        public boolean hasDrink(BlockPos center) {
            return StreamSupport.stream(BlockPos.iterateInSquare(center, 2, Direction.EAST, Direction.NORTH).spliterator(), false)
                    .map(pos -> entries.get(getIndex(pos)))
                    .anyMatch(list -> list != null && !list.empty());
        }

        public Optional<ItemStack> getDrink(BlockPos center) {
            return StreamSupport.stream(BlockPos.iterateInSquare(center, 2, Direction.EAST, Direction.NORTH).spliterator(), false)
                    .map(pos -> entries.get(getIndex(pos)))
                    .filter(list -> list != null && !list.empty())
                    .map(Stack::peek)
                    .map(Entry::stack)
                    .findFirst();
        }

        public TypedActionResult<ItemStack> placeDrink(BlockPos position, ItemStack stack, float yaw) {
            int index = getIndex(position);
            Stack<Entry> list = entries.get(index);
            if (list == null) {
                list = new Stack<>();
                entries.put(index, list);
            }
            if (list.size() < MAX_STACK_HEIGHT) {
                list.add(new Entry(position.getX() / 16F - 0.5F, position.getZ() / 16F - 0.5F, (-yaw) % 360, stack.split(1)));
                getWorld().playSound(null, getPos(), SoundEvents.BLOCK_CANDLE_PLACE, SoundCategory.BLOCKS);
                markDirty();
                return TypedActionResult.success(stack);
            }
            return TypedActionResult.fail(stack);
        }

        @Override
        public void readNbt(NbtCompound nbt, WrapperLookup lookup) {
            readEntriesFromNbt(nbt.getCompound("entries"));
        }

        @Override
        protected void writeNbt(NbtCompound nbt, WrapperLookup lookup) {
            nbt.put("entries", writeEntriesToNbt(new NbtCompound()));
        }

        private void readEntriesFromNbt(NbtCompound nbt) {
            entries.clear();
            nbt.getKeys().forEach(key -> {
                int index = Integer.parseInt(key);
                NbtList list = nbt.getList(key, NbtElement.COMPOUND_TYPE);
                if (!list.isEmpty()) {
                    entries.put(index, Entry.STACK_CODEC.decode(NbtOps.INSTANCE, list).getOrThrow().getFirst());
                }
            });
        }

        private NbtCompound writeEntriesToNbt(NbtCompound nbt) {
            entries.entries().forEach(entry -> {
                if (!entry.value().isEmpty()) {
                    Entry.STACK_CODEC.encodeStart(NbtOps.INSTANCE, entry.value()).result().ifPresent(stack -> {
                        nbt.put(entry.key() + "", stack);
                    });
                }
            });
            return nbt;
        }

        public static Optional<BlockPos> getHitPos(BlockHitResult hit) {
            Direction direction = hit.getSide();

            if (direction.getAxis() != Direction.Axis.Y) {
                return Optional.empty();
            }

            return Optional.of(getHitPos(hit.getBlockPos().offset(direction), hit.getPos()));
        }

        public static BlockPos getHitPos(BlockPos pos, Vec3d relativePos) {
            return BlockPos.ofFloored(
                    (relativePos.getX() - pos.getX()) * MAX_COORD,
                    0,
                    (relativePos.getZ() - pos.getZ()) * MAX_COORD
            );
        }

        private static int getIndex(BlockPos position) {
            return ((Math.max(0, position.getX()) * MAX_COORD) + Math.max(0, position.getZ())) % MAX_INDEX;
        }

        public record Entry (float x, float z, float rotation, ItemStack stack) {
            static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.FLOAT.fieldOf("x").forGetter(Entry::x),
                    Codec.FLOAT.fieldOf("z").forGetter(Entry::z),
                    Codec.FLOAT.fieldOf("rotation").forGetter(Entry::rotation),
                    ItemStack.CODEC.fieldOf("stack").forGetter(Entry::stack)
            ).apply(instance, Entry::new));
            static final Codec<Stack<Entry>> STACK_CODEC = CODEC.listOf().xmap(
                    list -> list.stream().collect(Collectors.toCollection(Stack::new)),
                    stack -> List.copyOf(stack)
            );
        }

        public interface DrinkConsumer {
            float accept(float y, Entry drink);
        }
    }
}

