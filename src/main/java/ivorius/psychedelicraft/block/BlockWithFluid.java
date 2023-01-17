/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.fluid.Resovoir;
import ivorius.psychedelicraft.item.FluidContainerItem;
import ivorius.psychedelicraft.screen.FluidContraptionScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.*;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * @author Sollace
 * @since 3 Jan 2023
 */
public abstract class BlockWithFluid<T extends BlockEntity & BlockWithFluid.DirectionalFluidResovoir> extends BlockWithEntity {
    protected BlockWithFluid(Settings settings) {
        super(settings);
    }

    protected abstract BlockEntityType<T> getBlockEntityType();

    protected abstract ScreenHandlerType<FluidContraptionScreenHandler<T>> getScreenHandlerType();

    public BlockPos getBlockEntityPos(BlockView world, BlockState state, BlockPos pos) {
        return pos;
    }

    @Override
    @Deprecated
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (!world.isClient && stack.getItem() instanceof FluidContainerItem container) {
            pos = getBlockEntityPos(world, state, pos);
            world.getBlockEntity(pos, getBlockEntityType()).ifPresent(be -> {
                be.getTank(Direction.UP).deposit(stack);
            });
            ((ServerWorld)world).getChunkManager().markForUpdate(pos);
        }
    }

    @Deprecated
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock()) && !world.isClient) {
            world.getBlockEntity(getBlockEntityPos(world, state, pos), getBlockEntityType()).ifPresent(be -> {
                be.onDestroyed((ServerWorld)world);
            });
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return world.getBlockEntity(getBlockEntityPos(world, state, pos), getBlockEntityType()).map(be -> {
            ActionResult result = onInteract(be.getCachedState(), world, be.getPos(), player, hand, be);
            if (result != ActionResult.PASS) {
                return result;
            }
            player.openHandledScreen(new ExtendedScreenHandlerFactory() {
                @Override
                public Text getDisplayName() {
                    return BlockWithFluid.this.getName();
                }

                @Override
                public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                    return new FluidContraptionScreenHandler<>(getScreenHandlerType(), syncId, inv, be, hit.getSide());
                }

                @Override
                public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                    buf.writeBlockPos(be.getPos());
                    buf.writeEnumConstant(hit.getSide());
                }
            });
            return ActionResult.SUCCESS;
        }).orElse(ActionResult.FAIL);
    }

    protected ActionResult onInteract(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, T blockEntity) {
        return ActionResult.PASS;
    }

    @Override
    @Nullable
    public <Q extends BlockEntity> BlockEntityTicker<Q> getTicker(World world, BlockState state, BlockEntityType<Q> type) {
        return world.isClient ? null : checkType(type, getBlockEntityType(), (w, p, s, entity) -> entity.tick((ServerWorld)w));
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return getBlockEntityType().instantiate(pos, state);
    }

    public interface DirectionalFluidResovoir {
        Resovoir getTank(Direction direction);

        void onDestroyed(ServerWorld world);

        void tick(ServerWorld world);
    }
}
