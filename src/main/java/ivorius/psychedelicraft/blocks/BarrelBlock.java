/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.blocks;

import ivorius.psychedelicraft.block.entity.BarrelBlockEntity;
import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import ivorius.psychedelicraft.fluids.FluidHelper;
import ivorius.psychedelicraft.items.FluidContainerItem;
import ivorius.psychedelicraft.screen.FluidContraptionScreenHandler;
import ivorius.psychedelicraft.screen.PSScreenHandlers;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class BarrelBlock extends BlockWithFluid<BarrelBlockEntity> {
    public static final int MAX_TAP_AMOUNT = FluidHelper.MILLIBUCKETS_PER_LITER;
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    public BarrelBlock(Settings settings) {
        super(settings.nonOpaque());
        setDefaultState(getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    @Deprecated
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite());
    }

    @Override
    protected ActionResult onInteract(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BarrelBlockEntity blockEntity) {

        ItemStack stack = player.getStackInHand(hand);
        if (stack.getItem() instanceof FluidContainerItem container) {

            if (container.getFluidLevel(stack) < container.getMaxCapacity(stack)) {
                if (stack.getCount() > 1) {
                    player.getInventory().offerOrDrop(blockEntity.getTank(Direction.DOWN).drain(MAX_TAP_AMOUNT, stack.split(1)));
                } else {
                    player.setStackInHand(hand, blockEntity.getTank(Direction.DOWN).drain(MAX_TAP_AMOUNT, stack.split(1)));
                }
            }

            blockEntity.timeLeftTapOpen = 20;
            blockEntity.markDirty();
            if (!world.isClient) {
                ((ServerWorld)world).getChunkManager().markForUpdate(pos);
            }

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    protected BlockEntityType<BarrelBlockEntity> getBlockEntityType() {
        return PSBlockEntities.BARREL;
    }

    @Override
    protected ScreenHandlerType<FluidContraptionScreenHandler<BarrelBlockEntity>> getScreenHandlerType() {
        return PSScreenHandlers.BARREL;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING);
    }
}
