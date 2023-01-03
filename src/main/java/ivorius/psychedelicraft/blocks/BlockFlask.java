/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.blocks;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.block.entity.*;
import ivorius.psychedelicraft.client.screen.ContainerFluidHandler;
import ivorius.psychedelicraft.fluids.Resovoir;
import ivorius.psychedelicraft.items.FluidContainerItem;
import ivorius.psychedelicraft.items.PSItems;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.*;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * Created by lukas on 25.10.14.
 */
public class BlockFlask extends BlockWithEntity {
    private static final VoxelShape SHAPE = Block.createCuboidShape(4, 0, 4, 12, 11.5, 12);

    public BlockFlask(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (!world.isClient && stack.getItem() instanceof FluidContainerItem container) {
            world.getBlockEntity(pos, PSBlockEntities.FLASK).ifPresent(be -> {
                be.getTank(Direction.UP).deposit(stack);
            });
        }
    }

    @Deprecated
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            world.getBlockEntity(pos, PSBlockEntities.FLASK).ifPresent(be -> {
                Resovoir tank = be.getTank(Direction.DOWN);
                ItemStack flaskStack = PSItems.itemFlask.getDefaultStack();
                int maxCapacity = PSItems.itemFlask.getMaxCapacity(flaskStack);
                while (!tank.isEmpty()) {
                    Block.dropStack(world, pos, tank.drain(maxCapacity, flaskStack));
                }
            });
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return world.getBlockEntity(pos, PSBlockEntities.FLASK).map(be -> {
            player.openHandledScreen(new ExtendedScreenHandlerFactory() {
                @Override
                public Text getDisplayName() {
                    return Text.translatable("container.flask");
                }
                @Override
                public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                    return new ContainerFluidHandler(null, syncId, inv, be.getTank(hit.getSide()));
                }

                @Override
                public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                }
            });
            return ActionResult.SUCCESS;
        }).orElse(ActionResult.FAIL);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : checkType(type, PSBlockEntities.FLASK, (w, p, s, entity) -> entity.tick((ServerWorld)w));
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new FlaskBlockEntity(pos, state);
    }
}
