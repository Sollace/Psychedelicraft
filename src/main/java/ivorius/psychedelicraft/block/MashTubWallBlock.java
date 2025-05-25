/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import java.util.*;

import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.block.entity.*;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Unit;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.*;

/**
 * Updated by Sollace on 7 Feb 2023
 */
public class MashTubWallBlock extends BlockWithEntity implements FluidFilled, PipeInsertable {
    public static final MapCodec<MashTubWallBlock> CODEC = createCodec(MashTubWallBlock::new);

    public MashTubWallBlock(Settings settings) {
        super(settings.luminance(LightBlock.STATE_TO_LUMINANCE));
    }

    @Override
    protected MapCodec<? extends MashTubWallBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(MashTubBlock.LIGHT);
    }

    @Override
    protected boolean isTransparent(BlockState state) {
        return true;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return getValidMasterPosition(world, pos)
                .map(center -> MashTubBlock.COLLISSION_SHAPE.offset(center.getX() - pos.getX(), 0, center.getZ() - pos.getZ()))
                .orElseGet(VoxelShapes::empty);
    }

    @Override
    protected VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        return getValidMasterPosition(world, pos)
                .map(center -> MashTubBlock.getShape(center.getX() - pos.getX() + 1, center.getZ() - pos.getZ() + 1))
                .orElseGet(VoxelShapes::empty);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    protected boolean hasSidedTransparency(BlockState state) {
        return true;
    }

    @Override
    protected float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        return 1;
    }

    @Override
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        return getValidMasterPosition(world, pos).map(center -> {
            BlockState masterState = world.getBlockState(center);
            return masterState.getPickStack(world, center, includeData);
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
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        return getValidMasterPosition(world, pos).map(p -> {
            return world.getBlockState(p).onUse(world, player, new BlockHitResult(hit.getPos(), hit.getSide(), p, hit.isInsideBlock()));
        }).orElse(ActionResult.PASS);
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return getValidMasterPosition(world, pos).map(p -> {
            return world.getBlockState(p).onUseWithItem(stack, world, player, hand, new BlockHitResult(hit.getPos(), hit.getSide(), p, hit.isInsideBlock()));
        }).orElse(ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION);
    }

    @Override
    protected boolean canBucketPlace(BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    public double getFluidHeight(World world, BlockState state, BlockPos pos) {
        return getValidMasterPosition(world, pos).map(center -> {
            BlockState masterState = world.getBlockState(center);
            return masterState.getBlock() instanceof FluidFilled vat ? vat.getFluidHeight(world, masterState, center) : -1D;
        }).orElse(-1D);
    }

    @Override
    public Optional<FluidState> getContainedFluid(World world, BlockState state, BlockPos pos) {
        return getValidMasterPosition(world, pos).flatMap(center -> {
            BlockState masterState = world.getBlockState(center);
            return masterState.getBlock() instanceof FluidFilled vat ? vat.getContainedFluid(world, masterState, center) : Optional.empty();
        });
    }

    @Override
    public boolean canFillWithFluid(@Nullable LivingEntity player, BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
        if (player == null) {
            return false;
        }
        return getValidMasterPosition(world, pos).filter(center -> {
            BlockState masterState = world.getBlockState(center);
            return masterState.getBlock() instanceof FluidFillable fillable && fillable.canFillWithFluid(player, world, center, masterState, fluid);
        }).isPresent();
    }

    @Override
    public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
        return getValidMasterPosition(world, pos).filter(center -> {
            BlockState masterState = world.getBlockState(center);
            return masterState.getBlock() instanceof FluidFillable fillable && fillable.tryFillWithFluid(world, center, masterState, fluidState);
        }).isPresent();
    }

    @Override
    public boolean acceptsConnectionFrom(WorldView world, BlockState state, BlockPos pos, BlockState neighborState, BlockPos neighborPos, Direction direction, boolean input) {
        return true;
    }

    @Override
    public Either<PipeFluids, Unit> tryInsert(ServerWorld world, BlockState state, BlockPos pos, Direction direction, PipeFluids fluids) {
        return getValidMasterPosition(world, pos).map(center -> PipeInsertable.tryInsert(world, center, direction, fluids)).orElse(PipeInsertable.reject(fluids));
    }

    @Override
    public Optional<PipeFluids> tryExtract(ServerWorld world, BlockState state, BlockPos pos, Direction direction) {
        return getValidMasterPosition(world, pos).flatMap(center -> PipeInsertable.tryExtract(world, center, direction));
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        getMasterPosition(world, pos).ifPresent(center -> {
            BlockState masterState = world.getBlockState(center);
            if (masterState.isOf(PSBlocks.MASH_TUB) || masterState.isOf(this)) {
                world.breakBlock(center, true);
            }
        });
        super.onStateReplaced(state, world, pos, moved);
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

    @Override
    protected boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return getValidMasterPosition(world, pos)
                .map(p -> world.getBlockState(p).getWeakRedstonePower(world, p, direction))
                .orElse(0);
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return getValidMasterPosition(world, pos)
                .map(p -> world.getBlockState(p).getComparatorOutput(world, p))
                .orElse(0);
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
        public void onBlockReplaced(BlockPos pos, BlockState oldState) {
            BlockState masterState = world.getBlockState(masterPos);
            if (masterState.isOf(PSBlocks.MASH_TUB) || masterState.isOf(PSBlocks.MASH_TUB_EDGE)) {
                world.breakBlock(masterPos, true);
            }
        }

        @Override
        public void writeNbt(NbtCompound compound, WrapperLookup lookup) {
            super.writeNbt(compound, lookup);
            compound.put("masterPos", BlockPos.CODEC, masterPos);
        }

        @Override
        public void readNbt(NbtCompound compound, WrapperLookup lookup) {
            super.readNbt(compound, lookup);
            masterPos = compound.get("masterPos", BlockPos.CODEC).orElse(BlockPos.ORIGIN);
        }
    }
}
