/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import java.util.*;

import ivorius.psychedelicraft.block.entity.*;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.*;

/**
 * Updated by Sollace on 7 Feb 2023
 */
public class MashTubWallBlock extends BlockWithEntity implements FluidFillable {
    public MashTubWallBlock(Settings settings) {
        super(settings.luminance(LightBlock.STATE_TO_LUMINANCE));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(MashTubBlock.LIGHT);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return getValidMasterPosition(world, pos)
                .map(center -> MashTubBlock.COLLISSION_SHAPE.offset(center.getX() - pos.getX(), 0, center.getZ() - pos.getZ()))
                .orElseGet(VoxelShapes::empty);
    }

    @Override
    @Deprecated
    public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        return MashTubBlock.RAYCAST_SHAPE;
    }

    @Override
    @Deprecated
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
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
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        return getValidMasterPosition(world, pos).map(center -> {
            BlockState masterState = world.getBlockState(center);
            return masterState.getBlock().getPickStack(world, center, masterState);
        }).orElse(ItemStack.EMPTY);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        getValidMasterPosition(world, pos).ifPresent(p -> {
            BlockState masterState = world.getBlockState(p);
            masterState.getBlock().randomDisplayTick(masterState, world, pos, random);
        });
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return getValidMasterPosition(world, pos).map(p -> {
            return world.getBlockState(p).onUse(world, player, hand, new BlockHitResult(hit.getPos(), hit.getSide(), p, hit.isInsideBlock()));
        }).orElse(ActionResult.PASS);
    }

    @Override
    public boolean canFillWithFluid(BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
        return getValidMasterPosition(world, pos).filter(center -> {
            BlockState masterState = world.getBlockState(center);
            return masterState.getBlock() instanceof FluidFillable fillable && fillable.canFillWithFluid(world, center, masterState, fluid);
        }).isPresent();
    }

    @Override
    public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
        return getValidMasterPosition(world, pos).filter(center -> {
            BlockState masterState = world.getBlockState(center);
            return masterState.getBlock() instanceof FluidFillable fillable && fillable.tryFillWithFluid(world, center, masterState, fluidState);
        }).isPresent();
    }

    @Deprecated
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock()) && !world.isClient) {
            getMasterPosition(world, pos).ifPresent(center -> {
                BlockState masterState = world.getBlockState(center);
                if (masterState.isOf(PSBlocks.MASH_TUB) || masterState.isOf(this)) {
                    world.breakBlock(center, true);
                }
            });
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    private Optional<BlockPos> getMasterPosition(BlockView world, BlockPos pos) {
        return world.getBlockEntity(pos, PSBlockEntities.MASH_TUB_EDGE).map(MasterPosition::getMasterPos).filter(p -> !p.equals(pos));
    }

    private Optional<BlockPos> getValidMasterPosition(BlockView world, BlockPos pos) {
        return getMasterPosition(world, pos).filter(p -> world.getBlockState(p).isOf(PSBlocks.MASH_TUB));
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new MasterPosition(pos, state);
    }

    public static class MasterPosition extends SyncedBlockEntity {

        private BlockPos masterPos;

        public MasterPosition(BlockPos pos, BlockState state) {
            super(PSBlockEntities.MASH_TUB_EDGE, pos, state);
            masterPos = pos;
        }

        public void setMasterPos(BlockPos pos) {
            masterPos = pos;
            markDirty();
            if (world instanceof ServerWorld sw) {
                sw.getChunkManager().markForUpdate(getPos());
            }
        }

        public BlockPos getMasterPos() {
            return masterPos;
        }

        @Override
        public void writeNbt(NbtCompound compound) {
            super.writeNbt(compound);
            compound.put("masterPos", NbtHelper.fromBlockPos(masterPos));
        }

        @Override
        public void readNbt(NbtCompound compound) {
            super.readNbt(compound);
            masterPos = NbtHelper.toBlockPos(compound.getCompound("masterPos"));
        }
    }
}
