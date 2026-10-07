/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.util.Untyped;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.TintedParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.*;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class JuniperLeavesBlock extends UntintedParticleLeavesBlock {
    public static final MapCodec<JuniperLeavesBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codecs.rangedInclusiveFloat(0, 1).fieldOf("leaf_particle_chance").forGetter(o -> o.leafParticleChance),
        ParticleTypes.TYPE_CODEC.fieldOf("leaf_particle").forGetter(o -> o.leafParticleEffect),
        createSettingsCodec()
    ).apply(i, JuniperLeavesBlock::new));

    public JuniperLeavesBlock(Settings settings) {
        this(0.1F, TintedParticleEffect.create(ParticleTypes.TINTED_LEAVES, 0xFF283D1F), settings);
    }

    public JuniperLeavesBlock(float particleChance, ParticleEffect leafParticleEffect, Settings settings) {
        super(particleChance, leafParticleEffect, settings);
    }

    @Override
    public MapCodec<UntintedParticleLeavesBlock> getCodec() {
        return Untyped.cast(CODEC);
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
