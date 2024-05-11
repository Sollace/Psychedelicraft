/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.block.entity.FlaskBlockEntity;
import ivorius.psychedelicraft.fluid.container.FluidContainer;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.screen.FluidContraptionScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.*;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.network.PacketByteBuf;
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
    public static final Identifier CONTENTS_DYNAMIC_DROP_ID = new Identifier("contents");

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
        if (!world.isClient && stack.getItem() instanceof FluidContainer) {
            world.getBlockEntity(pos, getBlockEntityType()).ifPresent(be -> {
                be.getTank(Direction.UP).deposit(stack);
            });
            ((ServerWorld)world).getChunkManager().markForUpdate(pos);
        }
    }

    @Deprecated
    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
        BlockEntity blockEntity = builder.getOptional(LootContextParameters.BLOCK_ENTITY);
        if (blockEntity instanceof DirectionalFluidResovoir container && blockEntity.getType() == getBlockEntityType()) {
            builder = builder.addDynamicDrop(CONTENTS_DYNAMIC_DROP_ID, lootConsumer -> {
                List<ItemStack> dynamicStacks = container.getDroppedStacks(FluidContainer.of(asItem()));
                if (dynamicStacks.isEmpty()) {
                    lootConsumer.accept(asItem().getDefaultStack());
                } else {
                    dynamicStacks.forEach(stack -> {
                        lootConsumer.accept(stack);
                    });
                }
            });
        }
        return super.getDroppedStacks(state, builder);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return world.getBlockEntity(pos, getBlockEntityType()).map(be -> {
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

        List<ItemStack> getDroppedStacks(FluidContainer container);

        void tick(ServerWorld world);
    }
}
