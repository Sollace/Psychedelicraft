/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import ivorius.psychedelicraft.particle.PSParticles;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/**
 * Created by lukas on 06.03.14.
 * Updated by Sollace on 3 Jan 2023
 */
public class GlitchedBlock extends BlockWithEntity {
    public static final MapCodec<GlitchedBlock> CODEC = createCodec(GlitchedBlock::new);

    public GlitchedBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends GlitchedBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient()) {
            world.removeBlock(pos, true);
        }
        return ActionResult.FAIL;
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler, boolean bl) {
        if (!world.isClient()) {
            world.removeBlock(pos, true);
        }
    }

    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        super.onLandedUpon(world, state, pos, entity, fallDistance);
        if (!world.isClient()) {
            world.removeBlock(pos, true);
        }
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (world.isAir(pos.up()) || world.isAir(pos.down()) || world.isAir(pos.east()) || world.isAir(pos.west()) || world.isAir(pos.north()) || world.isAir(pos.south())) {
            world.addParticleClient(PSParticles.BITS,
                    random.nextTriangular(pos.getX() + 0.5, 0.5),
                    random.nextTriangular(pos.getY() + 0.5, 0.5),
                    random.nextTriangular(pos.getZ() + 0.5, 0.5),
                    random.nextTriangular(0, 0.03),
                    0,
                    random.nextTriangular(0, 0.03)
            );
        }
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new Data(pos, state);
    }

    public static class Data extends BlockEntity {
        @Nullable
        private BlockState originalState;
        @Nullable
        private BlockEntity originalEntity;

        public Data(BlockPos pos, BlockState state) {
            super(PSBlockEntities.GLITCH, pos, state);
        }

        public void set(@Nullable BlockState state, @Nullable BlockEntity entity) {
            markDirty();
            if (state.isOf(this.getCachedState().getBlock())) {
                originalState = null;
                originalEntity = null;
                return;
            }
            originalState = state;
            originalEntity = entity;
        }

        public void restore(ServerWorld world) {
            if (originalState != null && !isRemoved()) {
                world.setBlockState(pos, originalState);
                if (originalEntity != null) {
                    originalEntity.cancelRemoval();
                    world.addBlockEntity(originalEntity);
                }
            }
            markRemoved();
        }

        @Override
        public void onBlockReplaced(BlockPos pos, BlockState oldState) {
            if (world instanceof ServerWorld sw) {
                restore(sw);
            }
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void readData(ReadView view) {
            originalState = view.read("originalState", BlockState.CODEC).orElse(null);
            if (originalState != null) {
                view.read("originalEntity", NbtCompound.CODEC).ifPresent(nbt -> {
                    originalEntity = BlockEntity.createFromNbt(pos, originalState, nbt, view.getRegistries());
                });
            }
        }

        @Override
        protected void writeData(WriteView view) {
            view.putNullable("originalState", BlockState.CODEC, originalState);
            if (originalEntity != null) {
                originalEntity.writeFullData(view.get("originalEntity"));
            }

        }
    }
}
