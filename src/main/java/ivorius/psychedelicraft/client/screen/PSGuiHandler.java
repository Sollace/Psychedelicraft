/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.screen;

import cpw.mods.fml.common.network.IGuiHandler;
import ivorius.psychedelicraft.block.entity.DryingTableBlockEntity;
import ivorius.psychedelicraft.blocks.*;
import ivorius.psychedelicraft.screen.DryingTableScreenHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.IFluidHandler;

/**
 * Created by lukas on 17.02.14.
 */
public class PSGuiHandler implements IGuiHandler
{
    public static final int dryingTableContainerID = 0;

    public static final int fluidHandlerContainerID_DOWN = 1;
    public static final int fluidHandlerContainerID_UP = 2;
    public static final int fluidHandlerContainerID_NORTH = 3;
    public static final int fluidHandlerContainerID_SOUTH = 4;
    public static final int fluidHandlerContainerID_WEST = 5;
    public static final int fluidHandlerContainerID_EAST = 6;

    public static final int barrelContainerID = 7;
    public static final int woodenVatContainerID = 8;
    public static final int distilleryContainerID = 9;

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        if (id == dryingTableContainerID)
            return new DryingTableScreenHandler(player.inventory, world, (DryingTableBlockEntity) world.getTileEntity(x, y, z));
        else if (id >= fluidHandlerContainerID_DOWN && id <= fluidHandlerContainerID_EAST)
        {
            ForgeDirection forgeDirection = ForgeDirection.getOrientation(id - fluidHandlerContainerID_DOWN);

            TileEntity tileEntity = world.getTileEntity(x, y, z);
            if (tileEntity instanceof IFluidHandler)
                return new ContainerFluidHandler(player.inventory, tileEntity, (IFluidHandler) tileEntity, forgeDirection);
        }
        else if (id == barrelContainerID)
        {
            TileEntity tileEntity = world.getTileEntity(x, y, z);
            if (tileEntity instanceof TileEntityBarrel)
                return new ContainerFluidHandler(player.inventory, tileEntity, (IFluidHandler) tileEntity, ForgeDirection.DOWN);
        }
        else if (id == woodenVatContainerID)
        {
            TileEntity tileEntity = world.getTileEntity(x, y, z);
            if (tileEntity instanceof TileEntityMashTub)
                return new ContainerFluidHandler(player.inventory, tileEntity, (IFluidHandler) tileEntity, ForgeDirection.DOWN);
        }
        else if (id == distilleryContainerID)
        {
            TileEntity tileEntity = world.getTileEntity(x, y, z);
            if (tileEntity instanceof TileEntityDistillery)
                return new ContainerFluidHandler(player.inventory, tileEntity, (IFluidHandler) tileEntity, ForgeDirection.DOWN);
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        if (id == dryingTableContainerID)
            return new GuiDryingTable(player.inventory, world, (DryingTableBlockEntity) world.getTileEntity(x, y, z));
        else if (id >= fluidHandlerContainerID_DOWN && id <= fluidHandlerContainerID_EAST)
        {
            ForgeDirection forgeDirection = ForgeDirection.getOrientation(id - fluidHandlerContainerID_DOWN);

            TileEntity tileEntity = world.getTileEntity(x, y, z);
            if (tileEntity instanceof IFluidHandler)
                return new GuiFluidHandler(player.inventory, tileEntity, (IFluidHandler) tileEntity, forgeDirection);
        }
        else if (id == barrelContainerID)
        {
            TileEntity tileEntity = world.getTileEntity(x, y, z);
            if (tileEntity instanceof TileEntityBarrel)
                return new GuiBarrel(player.inventory, (TileEntityBarrel) tileEntity);
        }
        else if (id == woodenVatContainerID)
        {
            TileEntity tileEntity = world.getTileEntity(x, y, z);
            if (tileEntity instanceof TileEntityMashTub)
                return new GuiWoodenVat(player.inventory, (TileEntityMashTub) tileEntity);
        }
        else if (id == distilleryContainerID)
        {
            TileEntity tileEntity = world.getTileEntity(x, y, z);
            if (tileEntity instanceof TileEntityDistillery)
                return new GuiDistillery(player.inventory, (TileEntityDistillery) tileEntity);
        }

        return null;
    }
}
