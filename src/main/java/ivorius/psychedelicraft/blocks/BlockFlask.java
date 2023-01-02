/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.blocks;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.block.entity.*;
import ivorius.psychedelicraft.client.screen.ContainerFluidHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created by lukas on 25.10.14.
 */
public class BlockFlask extends BlockWithEntity
{
    public BlockFlask(Settings settings) {
        super(settings);
        //setBlockBounds(0.25f, 0.0f, 0.25f, 0.75f, 0.7f, 0.75f);
    }
/*
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
    {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof FlaskBlockEntity)
        {
            if (!world.isRemote)
                player.openGui(Psychedelicraft.instance, PSGuiHandler.fluidHandlerContainerID_UP, world, x, y, z);

            return true;
        }

        return false;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack stack)
    {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof FlaskBlockEntity)
        {
            FlaskBlockEntity tileEntityFlask = (FlaskBlockEntity) tileEntity;

            FluidStack fluidStack = stack.getItem() instanceof IFluidContainerItem ? ((IFluidContainerItem) stack.getItem()).getFluid(stack) : null;
            if (fluidStack != null)
                tileEntityFlask.fill(ForgeDirection.UP, fluidStack, true);
        }
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest)
    {
        if (willHarvest)
        {
            TileEntity tileEntity = world.getTileEntity(x, y, z);
            if (tileEntity instanceof FlaskBlockEntity)
            {
                FlaskBlockEntity tileEntityFlask = (FlaskBlockEntity) tileEntity;
                FluidStack fluidStack = tileEntityFlask.drain(ForgeDirection.DOWN, FlaskBlockEntity.FLASK_CAPACITY, true);
                ItemStack stack = new ItemStack(this);

                if (fluidStack != null && fluidStack.amount > 0)
                    ((IFluidContainerItem) stack.getItem()).fill(stack, fluidStack, true);

                dropBlockAsItem(world, x, y, z, stack);
            }
        }

        return super.removedByPlayer(world, player, x, y, z, willHarvest);
    }
*/
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
        return new DryingTableBlockEntity(pos, state);
    }
}
