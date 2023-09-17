/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.*;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.loot.context.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.*;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;

public class BurdenedLatticeBlock extends LatticeBlock implements Fertilizable {
    public static final IntProperty AGE = Properties.AGE_3;
    public static final BooleanProperty PERSISTENT = Properties.PERSISTENT;
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
        setDefaultState(getDefaultState().with(AGE, 0).with(PERSISTENT, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(AGE, PERSISTENT);
    }

    @Override
    public BlockSoundGroup getSoundGroup(BlockState state) {
        if (state.get(AGE) > 0) {
            return BlockSoundGroup.GRASS;
        }
        return super.getSoundGroup(state);
    }

    @Override
    @Deprecated
    public List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
        ItemStack tool = builder.getOptional(LootContextParameters.TOOL);
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
                world.getServer().getLootManager().getLootTable(lootTableId)
                    .generateLoot(new LootContextParameterSet.Builder((ServerWorld)world)
                        .add(LootContextParameters.ORIGIN, Vec3d.ofCenter(pos))
                        .add(LootContextParameters.TOOL, player.getStackInHand(hand))
                        .add(LootContextParameters.BLOCK_STATE, state)
                        .addOptional(LootContextParameters.BLOCK_ENTITY, world.getBlockEntity(pos))
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
        }
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
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

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        ItemStack stack = super.getPickStack(world, pos, state);
        stack.setDamage(state.get(AGE));
        return stack;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return super.getPlacementState(ctx)
                .with(AGE, ctx.getStack().getDamage() % (MAX_AGE + 1))
                .with(PERSISTENT, true);
    }

    private void trySpread(BlockState state, World world, BlockPos pos) {
        if (!spreads) {
            return;
        }
        visitNeighbours(state, pos)
            .filter(mPos -> world.getBlockState(mPos).isOf(PSBlocks.LATTICE))
            .findFirst()
            .ifPresent(mPos -> world.setBlockState(mPos, copyStateProperties(getDefaultState(), world.getBlockState(mPos))));
    }

    private boolean canSpread(BlockState state, WorldView world, BlockPos pos) {
        return spreads && visitNeighbours(state, pos).anyMatch(mPos -> world.getBlockState(mPos).isOf(PSBlocks.LATTICE));
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

        if (getFreeConnections(state).anyMatch(facing -> world.getBlockState(pos.offset(facing)).isOf(stem))) {
            return true;
        }

        if (maxDepth > 1) {
            return visitNeighbours(state, pos)
                    .filter(visitedPositions::add)
                    .anyMatch(neighbourPos -> checkConnectivity(world, world.getBlockState(neighbourPos), neighbourPos, visitedPositions, maxDepth - 1));
        }

        return false;
    }

    private static Stream<BlockPos> visitNeighbours(BlockState state, BlockPos pos) {
        return getConnections(state).flatMap(d -> {
            BlockPos onLevel = pos.offset(d);
            return Stream.of(onLevel.up(), onLevel, onLevel.down());
        });
    }
}
