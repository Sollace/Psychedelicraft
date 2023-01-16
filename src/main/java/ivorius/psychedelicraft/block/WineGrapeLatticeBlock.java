/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import net.minecraft.block.*;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.loot.context.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;

public class WineGrapeLatticeBlock extends LatticeBlock implements Fertilizable {
    public static final IntProperty AGE = Properties.AGE_3;
    public static final int MAX_AGE = 3;

    public WineGrapeLatticeBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(AGE, 0));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (player.getStackInHand(hand).isOf(Items.SHEARS)) {

            if (state.get(AGE) < MAX_AGE) {
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
        return state.get(AGE) < MAX_AGE;
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        world.setBlockState(pos, state.with(AGE, MAX_AGE), Block.NOTIFY_ALL);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(AGE);
    }

}
