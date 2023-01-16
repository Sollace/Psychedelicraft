/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import java.util.List;

import ivorius.psychedelicraft.item.PSItems;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;

public class WineGrapeLatticeBlock extends LatticeBlock implements Fertilizable {
    public static final IntProperty AGE = Properties.AGE_3;
    public static final BooleanProperty PERSISTENT = Properties.PERSISTENT;
    public static final int MAX_AGE = 3;

    public WineGrapeLatticeBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(AGE, 0).with(PERSISTENT, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(AGE, PERSISTENT);
    }

    @Override
    @Deprecated
    public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder builder) {
        ItemStack tool = builder.getNullable(LootContextParameters.TOOL);
        if (tool != null && !tool.isEmpty() && EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, tool) > 0) {
            ItemStack drop = new ItemStack(PSItems.WINE_GRAPE_LATTICE, 1);
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
                world.setBlockState(pos, state.with(AGE, 1));
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
        if (world.getLightLevel(pos) >= 9 && random.nextInt(35) == 0) {
            if (isFertilizable(world, pos, state, false)) {
                world.setBlockState(pos, state.cycle(AGE), Block.NOTIFY_ALL);
            }
        }
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean client) {
        return state.get(AGE) < MAX_AGE && !state.get(PERSISTENT);
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return !state.get(PERSISTENT);
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        world.setBlockState(pos, state.with(AGE, MAX_AGE), Block.NOTIFY_ALL);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return super.getPlacementState(ctx)
                .with(AGE, ctx.getStack().getDamage() % (MAX_AGE + 1))
                .with(PERSISTENT, true);
    }
}
