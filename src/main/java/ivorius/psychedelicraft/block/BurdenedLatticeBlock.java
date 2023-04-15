/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.*;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.loot.context.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.*;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;

public class BurdenedLatticeBlock extends LatticeBlock implements Fertilizable {
    public static final IntProperty AGE = Properties.AGE_3;
    public static final BooleanProperty PERSISTENT = Properties.PERSISTENT;
    public static final BooleanProperty STABLE = BooleanProperty.of("stable");
    public static final int MAX_AGE = 3;

    private boolean spreads;

    @Nullable
    private final Block stem;

    private final int shearedAge;

    public BurdenedLatticeBlock(boolean spreads, @Nullable Block stem, int shearedAge, Settings settings) {
        super(settings);
        this.spreads = spreads;
        this.stem = stem;
        this.shearedAge = shearedAge;
        setDefaultState(getDefaultState().with(AGE, 0).with(PERSISTENT, false).with(STABLE, true));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(AGE, PERSISTENT, STABLE);
    }

    @Override
    @Deprecated
    public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder builder) {
        ItemStack tool = builder.getNullable(LootContextParameters.TOOL);
        if (tool != null && !tool.isEmpty() && EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, tool) > 0) {
            ItemStack drop = asItem().getDefaultStack();
            drop.setDamage(state.get(AGE));
            return List.of(drop);
        }
        return super.getDroppedStacks(state, builder);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (player.getStackInHand(hand).isOf(Items.SHEARS)) {

            if (state.get(AGE) < MAX_AGE || state.get(PERSISTENT)) {
                return ActionResult.FAIL;
            }

            world.playSoundFromEntity(null, player, SoundEvents.ENTITY_SHEEP_SHEAR, player.getSoundCategory(), 1, 1);

            if (!world.isClient) {
                Identifier lootTableId = getLootTableId().withPath(p -> p + "_farming");
                world.getServer().getLootManager().getTable(lootTableId)
                    .generateLoot(new LootContext.Builder((ServerWorld)world)
                        .random(world.random)
                        .parameter(LootContextParameters.ORIGIN, Vec3d.ofCenter(pos))
                        .parameter(LootContextParameters.TOOL, player.getStackInHand(hand))
                        .parameter(LootContextParameters.BLOCK_STATE, state)
                        .optionalParameter(LootContextParameters.BLOCK_ENTITY, world.getBlockEntity(pos))
                        .build(LootContextTypes.BLOCK)).forEach(stack -> {
                    Block.dropStack(world, pos, stack);
                });
                world.setBlockState(pos, state.with(AGE, shearedAge));
            }
            if (!player.isCreative()) {
                player.getStackInHand(hand).damage(1, player, p -> p.sendEquipmentBreakStatus(hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND));
            }
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(PERSISTENT)) {
            return;
        }

        if (checkConnectivity(world, state, pos)) {
            if (world.getLightLevel(pos) >= 9 && random.nextInt(35) == 0) {
                if (state.get(AGE) < MAX_AGE) {
                    world.setBlockState(pos, state.cycle(AGE), Block.NOTIFY_ALL);
                    world.updateNeighborsAlways(pos, this);
                }

                if (state.get(AGE) > 0) {
                    trySpread(state, world, pos);
                }
            }
        } else if (state.get(AGE) > 0) {
            world.setBlockState(pos, state.with(AGE, state.get(AGE) - 1), Block.NOTIFY_ALL);
            //world.scheduleBlockTick(pos, this, world.getRandom().nextBetween(2, 6));
        }
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!state.get(PERSISTENT) && state.get(AGE) > 0 && !state.get(STABLE)) {
          //  world.setBlockState(pos, state.with(AGE, state.get(AGE) - 1), Block.NOTIFY_ALL);
        }
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean client) {
        return (state.get(AGE) < MAX_AGE || canSpread(state, world, pos)) && !state.get(PERSISTENT);
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        if (state.get(AGE) < MAX_AGE) {
            world.setBlockState(pos, state.with(AGE, MAX_AGE), Block.NOTIFY_ALL);
        } else {
            trySpread(state, world, pos);
        }
    }

    @Deprecated
    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        state = super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
        //        .with(STABLE, checkConnectivity(world, state, pos));
        //if (!state.get(STABLE)) {
        //    world.scheduleBlockTick(pos, this, world.getRandom().nextBetween(2, 6));
        //}
        //System.out.println("Neighbour update stable=" + state.get(STABLE));
        return state;
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        ItemStack stack = super.getPickStack(world, pos, state);
        stack.setDamage(state.get(AGE));
        return stack;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState state = super.getPlacementState(ctx)
                .with(AGE, ctx.getStack().getDamage() % (MAX_AGE + 1))
                .with(PERSISTENT, true);
        return state;//.with(STABLE, checkConnectivity(ctx.getWorld(), state, ctx.getBlockPos()));
    }

    private void trySpread(BlockState state, World world, BlockPos pos) {
        if (!spreads) {
            return;
        }

        Direction facing = state.get(FACING);
        for (BlockPos mPos : iterateInPlane(state, pos)) {
            BlockState s = world.getBlockState(mPos);
            if (s.isOf(PSBlocks.LATTICE) && s.get(FACING).getAxis() == facing.getAxis()) {
                world.setBlockState(mPos, getDefaultState().with(FACING, s.get(FACING)));
                return;
            }
        }
    }

    private boolean canSpread(BlockState state, WorldView world, BlockPos pos) {
        if (!spreads) {
            return false;
        }

        Direction facing = state.get(FACING);
        for (BlockPos mPos : iterateInPlane(state, pos)) {
            BlockState s = world.getBlockState(mPos);
            if (s.isOf(PSBlocks.LATTICE) && s.get(FACING).getAxis() == facing.getAxis()) {
                return true;
            }
        }

        return false;
    }

    public boolean checkConnectivity(WorldView world, BlockState state, BlockPos pos) {
        if (stem == null) {
            return true;
        }

        Set<BlockPos> visitedPositions = new HashSet<>();
        visitedPositions.add(pos.toImmutable());
        return checkConnectivity(world, state, pos, visitedPositions, 20);
    }

    private boolean checkConnectivity(WorldView world, BlockState state, BlockPos pos, Set<BlockPos> visitedPositions, int maxDepth) {

        if (state.isOf(stem)) {
            return true;
        }

        if (!state.isOf(this)) {
            return false;
        }

        Direction facing = state.get(FACING);
        if (world.getBlockState(pos.offset(facing)).isOf(stem)
         || world.getBlockState(pos.offset(facing, -1)).isOf(stem)) {
            return true;
        }

        if (maxDepth > 1) {
            for (BlockPos.Mutable mPos : iterateInPlane(state, pos)) {
                BlockPos imPos = mPos.toImmutable();
                if (visitedPositions.add(imPos)) {
                    if (checkConnectivity(world, world.getBlockState(imPos), imPos, visitedPositions, maxDepth - 1)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static Iterable<BlockPos.Mutable> iterateInPlane(BlockState state, BlockPos pos) {
        Direction facing = state.get(FACING);
        return BlockPos.iterateInSquare(pos, 1, facing.rotateYClockwise(), Direction.UP);
    }
}
