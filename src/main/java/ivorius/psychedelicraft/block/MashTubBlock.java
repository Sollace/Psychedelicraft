/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import java.util.*;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.advancement.PSCriteria;
import ivorius.psychedelicraft.block.entity.*;
import ivorius.psychedelicraft.fluid.*;
import ivorius.psychedelicraft.screen.FluidContraptionScreenHandler;
import ivorius.psychedelicraft.screen.PSScreenHandlers;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.*;
import net.minecraft.fluid.*;
import net.minecraft.item.*;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.*;

/**
 * Created by lukas on 27.10.14.
 * Updated by Sollace on 12 Jan 2023
 */
public class MashTubBlock extends BlockWithFluid<MashTubBlockEntity> implements FluidFillable {
    public static final int SIZE = 15;
    public static final int BORDER_SIZE = 1;
    public static final int HEIGHT = 16;

    public static final BooleanProperty MASTER = BooleanProperty.of("master");

    private static final VoxelShape COLLISSION_SHAPE = VoxelShapes.union(
            createShape(-8, -0.5F, -8, 32, 16,  1),
            createShape(-8, -0.5F, 23, 32, 16,  1),
            createShape(23, -0.5F, -8,  1, 16, 32),
            createShape(-8, -0.5F, -8,  1, 16, 32),
            createShape(-8, -0.5F, -8, 32,  1, 32)
    );
    private static final VoxelShape RAYCAST_SHAPE = createShape(-8, -0.5F, -8, 32, 16, 32);

    private static VoxelShape createShape(double x, double y, double z, double width, double height, double depth) {
        return Block.createCuboidShape(x, y, z, x + width, y + height, z + depth);
    }

    public MashTubBlock(Settings settings) {
        super(settings);
    }

    @Override
    @Deprecated
    public BlockRenderType getRenderType(BlockState state) {
        if (!state.get(MashTubBlock.MASTER)) {
            return BlockRenderType.INVISIBLE;
        }
        return BlockRenderType.MODEL;
    }

    @Override
    public boolean hasSidedTransparency(BlockState state) {
        return true;
    }

    @Override
    public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        return state.get(MASTER) ? 0.2F : 1;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (state.get(MASTER)) {
            return COLLISSION_SHAPE;
        }
        BlockPos center = getBlockEntityPos(world, state, pos);
        if (center.equals(pos)) {
            return VoxelShapes.fullCube();
        }

        return COLLISSION_SHAPE.offset(center.getX() - pos.getX(), 0, center.getZ() - pos.getZ());
    }

    @Override
    @Deprecated
    public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        return state.get(MASTER) ? RAYCAST_SHAPE : VoxelShapes.empty();
    }

    @Override
    protected BlockEntityType<MashTubBlockEntity> getBlockEntityType() {
        return PSBlockEntities.MASH_TUB;
    }

    @Override
    protected ScreenHandlerType<FluidContraptionScreenHandler<MashTubBlockEntity>> getScreenHandlerType() {
        return PSScreenHandlers.MASH_TUB;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return super.getPlacementState(ctx).with(MASTER, true);
    }

    @Override
    protected ActionResult onInteract(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, MashTubBlockEntity blockEntity) {
        if (!blockEntity.solidContents.isEmpty()) {
            PSCriteria.SIMPLY_MASHING.trigger(player, blockEntity.solidContents);
            Block.dropStack(world, pos, blockEntity.solidContents);
            blockEntity.solidContents = ItemStack.EMPTY;
            blockEntity.markDirty();
            if (!world.isClient) {
                ((ServerWorld)world).getChunkManager().markForUpdate(pos);
            }
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return getPlacementPosition(world, state, pos, false).isPresent();
    }

    private Optional<BlockPos> getPlacementPosition(WorldView world, BlockState state, BlockPos pos, boolean premitOverlap) {
        return BlockPos.streamOutwards(pos, 1, 0, 1)
                .filter(center -> BlockPos.streamOutwards(center, 1, 0, 1).allMatch(p -> {
                    BlockState s = world.getBlockState(p);
                    return world.isAir(p) || s.isReplaceable() || (premitOverlap && s.isOf(this));
                }))
                .findFirst()
                .map(p -> p.toImmutable());
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        getPlacementPosition(world, state, pos, true).ifPresent(center -> {
            BlockPos.iterateOutwards(center, 1, 0, 1).forEach(p -> {
                world.setBlockState(p, getDefaultState().with(MASTER, false));
                world.removeBlockEntity(p);
            });
            world.setBlockState(center, getDefaultState().with(MASTER, true));
        });

        super.onPlaced(world, pos, state, placer, stack);
    }

    @Override
    public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
        BlockPos center = getBlockEntityPos(world, state, pos);
        if (center.equals(pos) && !state.get(MASTER)) {
            return;
        }
        BlockPos.iterateOutwards(center, 1, 0, 1).forEach(p -> {
            if (world.getBlockState(p).isOf(this)) {
                world.setBlockState(p, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
            }
        });
    }

    @Override
    public BlockPos getBlockEntityPos(BlockView world, BlockState state, BlockPos pos) {
        return BlockPos.findClosest(pos, 1, 0, p -> {
            BlockState s = world.getBlockState(p);
            return s.isOf(this) && s.get(MASTER);
         }).orElse(pos);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        if (!state.get(MASTER)) {
            return null;
        }
        return super.createBlockEntity(pos, state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(MASTER);
    }

    public FluidState getFluidState(World world, BlockState state, BlockPos pos) {
        if (state.get(MASTER) && world.getBlockEntity(pos, getBlockEntityType()).filter(be -> !be.getTank(Direction.UP).isEmpty()).isPresent()) {
            return Fluids.WATER.getDefaultState();
        }
        return Fluids.EMPTY.getDefaultState();
    }

    @Override
    public boolean canFillWithFluid(BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
        return world.getBlockEntity(getBlockEntityPos(world, state, pos), getBlockEntityType()).filter(be -> {
            Resovoir tank = be.getTank(Direction.UP);
            return (tank.isEmpty()
                || tank.getFluidType().getFluidState(0).isOf(fluid))
                && tank.getCapacity() - tank.getLevel() >=  FluidHelper.MILLIBUCKETS_PER_LITER;
        }).isPresent();
    }

    @Override
    public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
        return world.getBlockEntity(getBlockEntityPos(world, state, pos), getBlockEntityType()).filter(be -> {
            SimpleFluid f = SimpleFluid.forVanilla(fluidState.getFluid());

            Resovoir tank = be.getTank(Direction.UP);

            if (tank.getCapacity() - tank.getLevel() <  FluidHelper.MILLIBUCKETS_PER_LITER) {
                return false;
            }

            ItemStack overflow = tank.deposit(f.getDefaultStack(FluidHelper.MILLIBUCKETS_PER_LITER));
            if (!FluidContainerItem.DEFAULT.getFluid(overflow).isEmpty()) {
                if (world instanceof World) {
                    Block.dropStack((World)world, pos, overflow);
                }
            }

            if (world instanceof ServerWorld sw) {
                sw.getChunkManager().markForUpdate(pos);
                be.markDirty();
            }
            return true;
        }).isPresent();
    }
}
