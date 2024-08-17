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
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
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
    @Deprecated
    public BlockRenderType getRenderType(BlockState state) {
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

    @Deprecated
    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
        appendDroppedStacks(asItem().getDefaultStack(), state, builder);
        return super.getDroppedStacks(state, builder);
    }

    public static void appendDroppedStacks(ItemStack defaultStack, BlockState state, LootContextParameterSet.Builder builder) {
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
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
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
    public ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return world.getBlockEntity(pos, getBlockEntityType()).map(be -> {
            return onInteractWithItem(stack, be.getCachedState(), world, be.getPos(), player, hand, be);
        }).orElse(ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
    }

    protected ItemActionResult onInteractWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, T blockEntity) {
        return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    protected ActionResult onInteract(BlockState state, World world, BlockPos pos, PlayerEntity player, T blockEntity) {
        return ActionResult.PASS;
    }

    @Override
    @Nullable
    public <Q extends BlockEntity> BlockEntityTicker<Q> getTicker(World world, BlockState state, BlockEntityType<Q> type) {
        return world.isClient ? null : validateTicker(type, getBlockEntityType(), (w, p, s, entity) -> entity.tick((ServerWorld)w));
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return getBlockEntityType().instantiate(pos, state);
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
