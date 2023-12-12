/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.item.PSItems;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class JuniperLeavesBlock extends LeavesBlock {
    public static final MapCodec<JuniperLeavesBlock> CODEC = createCodec(JuniperLeavesBlock::new);

    public JuniperLeavesBlock(Settings settings) {
        super(settings);
    }

    @Override
    public MapCodec<? extends JuniperLeavesBlock> getCodec() {
        return CODEC;
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return !state.get(PERSISTENT);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.randomTick(state, world, pos, random);
        if (this == PSBlocks.JUNIPER_LEAVES && random.nextFloat() < 0.01F && !state.get(WATERLOGGED)) {
            world.setBlockState(pos, PSBlocks.FRUITING_JUNIPER_LEAVES.getDefaultState()
                    .with(DISTANCE, state.get(DISTANCE))
                    .with(PERSISTENT, state.get(PERSISTENT))
                    .with(WATERLOGGED, state.get(WATERLOGGED)));
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (this == PSBlocks.FRUITING_JUNIPER_LEAVES) {
            Block.dropStack(world, pos, PSItems.JUNIPER_BERRIES.getDefaultStack());
            world.setBlockState(pos, PSBlocks.JUNIPER_LEAVES.getDefaultState()
                    .with(DISTANCE, state.get(DISTANCE))
                    .with(PERSISTENT, state.get(PERSISTENT))
                    .with(WATERLOGGED, state.get(WATERLOGGED)));
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

}
