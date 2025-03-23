/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.item.PSItems;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.*;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class JuniperLeavesBlock extends TintedParticleLeavesBlock {
    public static final MapCodec<JuniperLeavesBlock> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codecs.rangedInclusiveFloat(0.0F, 1.0F)
                        .fieldOf("leaf_particle_chance")
                        .forGetter(tintedParticleLeavesBlock -> tintedParticleLeavesBlock.leafParticleChance),
                    createSettingsCodec()
                )
                .apply(instance, JuniperLeavesBlock::new)
        );

    public JuniperLeavesBlock(Settings settings) {
        this(0.1F, settings);
    }

    public JuniperLeavesBlock(float particleChance, Settings settings) {
        super(particleChance, settings);
    }

    @Override
    public MapCodec<? extends JuniperLeavesBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return !state.get(PERSISTENT);
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.randomTick(state, world, pos, random);
        if (this == PSBlocks.JUNIPER_LEAVES && random.nextFloat() < 0.01F && !state.get(WATERLOGGED)) {
            world.setBlockState(pos, PSBlocks.FRUITING_JUNIPER_LEAVES.getDefaultState()
                    .with(DISTANCE, state.get(DISTANCE))
                    .with(PERSISTENT, state.get(PERSISTENT))
                    .with(WATERLOGGED, state.get(WATERLOGGED)));
        }
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
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
