/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.block.entity.FlaskBlockEntity;
import ivorius.psychedelicraft.fluid.Processable;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.screen.FluidContraptionScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.screen.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * @author Sollace
 * @since 3 Jan 2023
 */
public abstract class BlockWithFluid<T extends FlaskBlockEntity> extends BlockWithEntity {
    public static final Identifier CONTENTS_DYNAMIC_DROP_ID = Identifier.ofVanilla("contents");

    protected BlockWithFluid(Settings settings) {
        super(settings);
    }

    protected abstract BlockEntityType<T> getBlockEntityType();

    protected abstract ScreenHandlerType<FluidContraptionScreenHandler<T>> getScreenHandlerType();

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (!world.isClient && FluidCapacity.get(stack) > 0) {
            world.getBlockEntity(pos, getBlockEntityType()).ifPresent(be -> {
                be.getPrimaryTank().deposit(stack);
            });
            ((ServerWorld)world).getChunkManager().markForUpdate(pos);
        }
    }

    @Override
    protected List<ItemStack> getDroppedStacks(BlockState state, LootWorldContext.Builder builder) {
        appendDroppedStacks(asItem().getDefaultStack(), state, builder);
        return super.getDroppedStacks(state, builder);
    }

    @Deprecated
    public static void appendDroppedStacks(ItemStack defaultStack, BlockState state, LootWorldContext.Builder builder) {
        BlockEntity blockEntity = builder.getOptional(LootContextParameters.BLOCK_ENTITY);
        if (blockEntity instanceof DirectionalFluidResovoir container) {
            builder = builder.addDynamicDrop(CONTENTS_DYNAMIC_DROP_ID, lootConsumer -> {
                List<ItemStack> dynamicStacks = container.getDroppedStacks(defaultStack);
                if (dynamicStacks.isEmpty()) {
                    if (!defaultStack.isEmpty()) {
                        lootConsumer.accept(defaultStack);
                    }
                } else {
                    dynamicStacks.forEach(stack -> {
                        lootConsumer.accept(stack);
                    });
                }
            });
        }
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        return world.getBlockEntity(pos, getBlockEntityType()).map(be -> {
            ActionResult result = onInteract(be.getCachedState(), world, be.getPos(), player, be);
            if (result != ActionResult.PASS) {
                return result;
            }
            player.openHandledScreen(new ExtendedScreenHandlerFactory<InteractionData>() {
                @Override
                public Text getDisplayName() {
                    return BlockWithFluid.this.getName();
                }

                @Override
                public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                    return new FluidContraptionScreenHandler<>(getScreenHandlerType(), syncId, inv, be, hit.getSide());
                }

                @Override
                public InteractionData getScreenOpeningData(ServerPlayerEntity player) {
                    return new InteractionData(be.getPos(), hit.getSide());
                }
            });
            return ActionResult.SUCCESS;
        }).orElse(ActionResult.FAIL);
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return world.getBlockEntity(pos, getBlockEntityType()).map(be -> {
            return onInteractWithItem(stack, be.getCachedState(), world, be.getPos(), player, hand, be);
        }).orElse(ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION);
    }

    protected ActionResult onInteractWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, T blockEntity) {
        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }

    protected ActionResult onInteract(BlockState state, World world, BlockPos pos, PlayerEntity player, T blockEntity) {
        return ActionResult.PASS;
    }

    @Override
    @Nullable
    public final <Q extends BlockEntity> BlockEntityTicker<Q> getTicker(World world, BlockState state, BlockEntityType<Q> type) {
        return world.isClient
                ? validateTicker(type, getBlockEntityType(), (w, p, s, entity) -> entity.clientTick(w))
                : validateTicker(type, getBlockEntityType(), (w, p, s, entity) -> entity.tick((ServerWorld)w));
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return getBlockEntityType().instantiate(pos, state);
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return toRedstoneSignal(world.getBlockEntity(pos, getBlockEntityType())
                .map(FlaskBlockEntity::getPrimaryTank)
                .map(tank -> MathHelper.clamp(tank.getAmount() / (float)tank.getCapacity(), 0, 1))
                .orElse(0F), 15);
    }

    static int toRedstoneSignal(float percentage, int maxSignal) {
        return percentage > 0 ? MathHelper.clamp((int)(percentage * maxSignal), 1, maxSignal) : 0;
    }

    public record InteractionData(BlockPos pos, Direction side) {
        public static final PacketCodec<PacketByteBuf, InteractionData> PACKET_CODEC = PacketCodec.tuple(
                BlockPos.PACKET_CODEC, InteractionData::pos,
                Direction.PACKET_CODEC, InteractionData::side,
                InteractionData::new
        );
    }

    public interface DirectionalFluidResovoir extends SidedStorageBlockEntity, SidedInventory, Processable.Context {

        List<ItemStack> getDroppedStacks(ItemStack container);

        void tick(ServerWorld world);

        void clientTick(World world);

        @Override
        default Storage<FluidVariant> getFluidStorage(Direction side) {
            return getTankOnSide(side);
        }

        @Override
        default Storage<ItemVariant> getItemStorage(@Nullable Direction side) {
            return InventoryStorage.of(this, side);
        }
    }
}
