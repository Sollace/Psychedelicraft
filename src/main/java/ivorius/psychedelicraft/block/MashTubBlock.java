/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.advancement.PSCriteria;
import ivorius.psychedelicraft.block.entity.*;
import ivorius.psychedelicraft.fluid.*;
import ivorius.psychedelicraft.fluid.container.FluidContainer;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.MashTubItem;
import ivorius.psychedelicraft.screen.FluidContraptionScreenHandler;
import ivorius.psychedelicraft.screen.PSScreenHandlers;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.fluid.*;
import net.minecraft.item.*;
import net.minecraft.tag.FluidTags;
import net.minecraft.tag.TagKey;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
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

    public static final IntProperty LIGHT = Properties.LEVEL_15;

    static final VoxelShape COLLISSION_SHAPE = VoxelShapes.union(
            createShape(-8, -0.5F, -8, 32, 16,  1),
            createShape(-8, -0.5F, 23, 32, 16,  1),
            createShape(23, -0.5F, -8,  1, 16, 32),
            createShape(-8, -0.5F, -8,  1, 16, 32),
            createShape(-8, -0.5F, -8, 32,  1, 32)
    );
    static final VoxelShape RAYCAST_SHAPE = createShape(-8, -0.5F, -8, 32, 16, 32);

    private static VoxelShape createShape(double x, double y, double z, double width, double height, double depth) {
        return Block.createCuboidShape(x, y, z, x + width, y + height, z + depth);
    }

    public MashTubBlock(Settings settings) {
        super(settings.luminance(LightBlock.STATE_TO_LUMINANCE));
    }

    @Override
    @Deprecated
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public boolean hasSidedTransparency(BlockState state) {
        return true;
    }

    @Override
    public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        return 0.2F;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return COLLISSION_SHAPE;
    }

    @Override
    @Deprecated
    public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        return RAYCAST_SHAPE;
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
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        world.getBlockEntity(getBlockEntityPosition(world, pos), getBlockEntityType()).ifPresent(be -> {
            SimpleFluid fluid = be.getTank(Direction.UP).getFluidType();
            fluid.randomDisplayTick(world, pos, fluid.getPhysical().getDefaultState(), random);
        });
    }

    @Override
    protected ActionResult onInteract(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, MashTubBlockEntity blockEntity) {
        ItemStack heldStack = player.getStackInHand(hand);
        if (!heldStack.isEmpty()) {
            TypedActionResult<ItemStack> result = blockEntity.depositIngredient(heldStack.copy());
            if (!player.isCreative()) {
                player.setStackInHand(hand, result.getValue());
            }
            if (result.getResult().isAccepted()) {
                return result.getResult();
            }
        }

        if (!blockEntity.solidContents.isEmpty()) {
            PSCriteria.SIMPLY_MASHING.trigger(player, blockEntity.solidContents);
            Block.dropStack(world, pos, blockEntity.solidContents);
            blockEntity.solidContents = ItemStack.EMPTY;
            blockEntity.markForUpdate();
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return MashTubItem.findPlacementPosition(world, pos).isPresent();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LIGHT);
    }

    public int getFluidHeight(World world, BlockState state, BlockPos pos, TagKey<Fluid> tag) {
        return world.getBlockEntity(pos, getBlockEntityType())
                .map(be -> be.getTank(Direction.UP))
                .filter(tank -> tank.getFluidType().getPhysical().isIn(tag) || (tag == FluidTags.WATER && tank.getFluidType().isCustomFluid()))
                .map(tank -> (int)(((float)tank.getLevel() / tank.getCapacity()) * 8))
                .orElse(-1);
    }

    @Override
    public boolean canFillWithFluid(BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
        return world.getBlockEntity(pos, getBlockEntityType()).filter(be -> {
            Resovoir tank = be.getTank(Direction.UP);
            return (tank.isEmpty()
                || tank.getFluidType().getPhysical().isOf(fluid))
                && tank.getCapacity() - tank.getLevel() >=  FluidVolumes.BUCKET;
        }).isPresent();
    }

    @Deprecated
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock()) && !world.isClient) {
            BlockPos.iterateOutwards(pos, 1, 0, 1).forEach(p -> {
                if (!p.equals(pos)) {
                    BlockState neighbourState = world.getBlockState(p);
                    if (neighbourState.isOf(PSBlocks.MASH_TUB_EDGE)) {
                        world.removeBlockEntity(p);
                        world.setBlockState(p, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                    }
                }
            });
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
        return world.getBlockEntity(getBlockEntityPosition(world, pos), getBlockEntityType()).filter(be -> {
            SimpleFluid f = SimpleFluid.forVanilla(fluidState.getFluid());

            Resovoir tank = be.getTank(Direction.UP);

            if (tank.getCapacity() - tank.getLevel() <  FluidVolumes.BUCKET) {
                return false;
            }

            ItemStack overflow = tank.deposit(f.getDefaultStack(FluidVolumes.BUCKET));
            if (!FluidContainer.UNLIMITED.getFluid(overflow).isEmpty()) {
                if (world instanceof World) {
                    Block.dropStack((World)world, pos, overflow);
                }
            }

            be.markForUpdate();
            return true;
        }).isPresent();
    }

    protected BlockPos getBlockEntityPosition(BlockView world, BlockPos pos) {
        return world.getBlockEntity(pos, PSBlockEntities.MASH_TUB_EDGE).map(p -> p.getMasterPos()).orElse(pos);
    }

    @Override
    @Nullable
    public <Q extends BlockEntity> BlockEntityTicker<Q> getTicker(World world, BlockState state, BlockEntityType<Q> type) {
        return world.isClient ? checkType(type, getBlockEntityType(), (w, p, s, entity) -> entity.tickAnimations()) : super.getTicker(world, state, type);
    }
}
