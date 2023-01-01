/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.screen;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidHandler;

/**
 * Created by lukas on 26.10.14.
 */
public class ContainerFluidHandler extends Container implements UpdatableContainer
{
    public IInventory fluidIOInventory = new InventoryBasic("FluidIO", true, 1)
    {
        public int getInventoryStackLimit()
        {
            return 1;
        }

        public void markDirty()
        {
            super.markDirty();
            ContainerFluidHandler.this.onCraftMatrixChanged(this);
        }
    };

    public InventoryPlayer inventoryPlayer;

    public TileEntity tileEntity;
    public IFluidHandler fluidHandler;
    public ForgeDirection side;

    public int drainSpeedPerTick = 100;
    public boolean currentlyDrainingItem;

    public ContainerFluidHandler(InventoryPlayer inventoryPlayer, TileEntity tileEntity, IFluidHandler fluidHandler, ForgeDirection side)
    {
        this.inventoryPlayer = inventoryPlayer;
        this.tileEntity = tileEntity;
        this.fluidHandler = fluidHandler;
        this.side = side;

        this.addSlotToContainer(new Slot(this.fluidIOInventory, 0, 25, 40));
        for (int y = 0; y < 3; ++y)
        {
            for (int x = 0; x < 9; ++x)
            {
                this.addSlotToContainer(new Slot(inventoryPlayer, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
            }
        }

        for (int x = 0; x < 9; ++x)
        {
            this.addSlotToContainer(new Slot(inventoryPlayer, x, 8 + x * 18, 142));
        }
    }

    @Override
    public void updateAsCustomContainer()
    {
        transferLiquid(currentlyDrainingItem, drainSpeedPerTick);
    }

    public int transferLiquid(boolean drainItem, int drainSpeed)
    {
        ItemStack ioStack = fluidIOInventory.getStackInSlot(0);
        if (ioStack != null && ioStack.getItem() instanceof IFluidContainerItem)
        {
            IFluidContainerItem fluidContainerItem = (IFluidContainerItem) ioStack.getItem();
            if (drainItem)
            {
                FluidStack drainedSim = fluidContainerItem.drain(ioStack, drainSpeed, false);
                int maxFill = fluidHandler.fill(side, drainedSim, false);

                FluidStack drained = fluidContainerItem.drain(ioStack, maxFill, true);
                return fluidHandler.fill(side, drained, true);
            }
            else
            {
                FluidStack drainedSim = fluidHandler.drain(side, drainSpeed, false);
                int maxFill = fluidContainerItem.fill(ioStack, drainedSim, false);

                FluidStack drained = fluidHandler.drain(side, maxFill, true);
                return fluidContainerItem.fill(ioStack, drained, true);
            }
        }

        return 0;
    }

    @Override
    public boolean enchantItem(EntityPlayer player, int action)
    {
        if (action == 1 || action == 0)
        {
            currentlyDrainingItem = action == 1;
            return true;
        }

        return super.enchantItem(player, action);
    }

    public void onContainerClosed(EntityPlayer p_75134_1_)
    {
        super.onContainerClosed(p_75134_1_);

        if (!this.tileEntity.getWorldObj().isRemote)
        {
            ItemStack itemstack = this.fluidIOInventory.getStackInSlotOnClosing(0);

            if (itemstack != null)
                p_75134_1_.dropPlayerItemWithRandomChoice(itemstack, false);
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player)
    {
        return tileEntity.getWorldObj().getBlock(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord) == tileEntity.getBlockType() && player.getDistanceSq((double) tileEntity.xCoord + 0.5D, (double) tileEntity.yCoord + 0.5D, (double) tileEntity.zCoord + 0.5D) <= 64.0D;
    }

    public ItemStack transferStackInSlot(EntityPlayer p_82846_1_, int p_82846_2_)
    {
        ItemStack itemstack = null;
        Slot slot = (Slot) this.inventorySlots.get(p_82846_2_);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (p_82846_2_ == 0)
            {
                if (!this.mergeItemStack(itemstack1, 1, 37, true))
                {
                    return null;
                }
            }
            else
            {
                if (((Slot) this.inventorySlots.get(0)).getHasStack() || !((Slot) this.inventorySlots.get(0)).isItemValid(itemstack1))
                {
                    return null;
                }

                if (itemstack1.hasTagCompound() && itemstack1.stackSize == 1)
                {
                    ((Slot) this.inventorySlots.get(0)).putStack(itemstack1.copy());
                    itemstack1.stackSize = 0;
                }
                else if (itemstack1.stackSize >= 1)
                {
                    ((Slot) this.inventorySlots.get(0)).putStack(new ItemStack(itemstack1.getItem(), 1, itemstack1.getItemDamage()));
                    --itemstack1.stackSize;
                }
            }

            if (itemstack1.stackSize == 0)
            {
                slot.putStack(null);
            }
            else
            {
                slot.onSlotChanged();
            }

            if (itemstack1.stackSize == itemstack.stackSize)
            {
                return null;
            }

            slot.onPickupFromSlot(p_82846_1_, itemstack1);
        }

        return itemstack;
    }
}
