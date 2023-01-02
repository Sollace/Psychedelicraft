/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.screen;

import ivorius.psychedelicraft.fluids.Resovoir;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

/**
 * Created by lukas on 26.10.14.
 */
public class ContainerFluidHandler extends ScreenHandler implements UpdatableContainer {

    public int drainSpeedPerTick = 100;
    public boolean currentlyDrainingItem;

    private final Resovoir tank;

    public ContainerFluidHandler(ScreenHandlerType<? extends ContainerFluidHandler> type, int syncId, PlayerInventory inventoryPlayer, Resovoir tank) {
        super(type, syncId);
        this.tank = tank;
        addSlot(new Slot(tank, 0, 25, 40) {
            @Override
            public void markDirty() {
                super.markDirty();
                ContainerFluidHandler.this.onContentChanged(inventory);
            }
        });
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                addSlot(new Slot(inventoryPlayer, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
            }
        }

        for (int x = 0; x < 9; ++x) {
            addSlot(new Slot(inventoryPlayer, x, 8 + x * 18, 142));
        }
    }

    @Override
    public void updateAsCustomContainer()
    {
        transferLiquid(currentlyDrainingItem, drainSpeedPerTick);
    }

    public int transferLiquid(boolean drainItem, int drainSpeed)
    {
        ItemStack ioStack = tank.getStack();
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
    public boolean enchantItem(PlayerEntity player, int action)
    {
        if (action == 1 || action == 0)
        {
            currentlyDrainingItem = action == 1;
            return true;
        }

        return super.enchantItem(player, action);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return tileEntity.getWorldObj().getBlock(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord) == tileEntity.getBlockType()
            && player.getDistanceSq((double) tileEntity.xCoord + 0.5D, (double) tileEntity.yCoord + 0.5D, (double) tileEntity.zCoord + 0.5D) <= 64.0D;
    }

    @Override
    public ItemStack quickMove(PlayerEntity p_82846_1_, int p_82846_2_)
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
