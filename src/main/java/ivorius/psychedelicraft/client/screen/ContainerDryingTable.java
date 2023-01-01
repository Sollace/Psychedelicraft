package ivorius.psychedelicraft.client.screen;

import ivorius.psychedelicraft.blocks.PSBlocks;
import ivorius.psychedelicraft.blocks.SlotDryingTableResult;
import ivorius.psychedelicraft.blocks.TileEntityDryingTable;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
* Created by lukas on 08.11.14.
*/
public class ContainerDryingTable extends Container
{
    private World worldObj;
    public TileEntityDryingTable tileEntityDryingTable;

    public ContainerDryingTable(InventoryPlayer inventoryPlayer, World world, TileEntityDryingTable dryingTable)
    {
        this.worldObj = world;
        this.tileEntityDryingTable = dryingTable;
        this.addSlotToContainer(new SlotDryingTableResult(inventoryPlayer.player, dryingTable, 0, 124, 35));

        for (int x = 0; x < 3; ++x)
        {
            for (int y = 0; y < 3; ++y)
            {
                this.addSlotToContainer(new Slot(dryingTable, 1 + x * 3 + y, 30 + x * 18, 17 + y * 18));
            }
        }

        int var3;

        for (var3 = 0; var3 < 3; ++var3)
        {
            for (int var4 = 0; var4 < 9; ++var4)
            {
                this.addSlotToContainer(new Slot(inventoryPlayer, var4 + var3 * 9 + 9, 8 + var4 * 18, 84 + var3 * 18));
            }
        }

        for (var3 = 0; var3 < 9; ++var3)
        {
            this.addSlotToContainer(new Slot(inventoryPlayer, var3, 8 + var3 * 18, 142));
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer player)
    {
        super.onContainerClosed(player);

        if (!this.worldObj.isRemote)
        {
            for (int var2 = 0; var2 < 9; ++var2)
            {
                ItemStack var3 = this.tileEntityDryingTable.getStackInSlotOnClosing(var2);

                if (var3 != null)
                {
                    player.dropPlayerItemWithRandomChoice(var3, false);
                }
            }
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player)
    {
        Block id = this.worldObj.getBlock(tileEntityDryingTable.xCoord, tileEntityDryingTable.yCoord, tileEntityDryingTable.zCoord);
        return (id == PSBlocks.dryingTable || id == PSBlocks.dryingTableIron) && player.getDistanceSq(tileEntityDryingTable.xCoord + 0.5D, tileEntityDryingTable.yCoord + 0.5D, tileEntityDryingTable.zCoord + 0.5D) <= 64.0D;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slot)
    {
        ItemStack var2 = null;
        Slot var3 = (Slot) this.inventorySlots.get(slot);

        if (var3 != null && var3.getHasStack())
        {
            ItemStack var4 = var3.getStack();
            var2 = var4.copy();

            if (slot < 10)
            {
                if (!this.mergeItemStack(var4, 10, 46, false))
                {
                    return null;
                }

                var3.onSlotChange(var4, var2);
            }
            else if (slot >= 10 && slot < 37)
            {
                if (!this.mergeItemStack(var4, 37, 46, false))
                {
                    return null;
                }
            }
            else if (slot >= 37 && slot < 46)
            {
                if (!this.mergeItemStack(var4, 10, 37, false))
                {
                    return null;
                }
            }
            else if (!this.mergeItemStack(var4, 10, 37, false))
            {
                return null;
            }

            if (var4.stackSize == 0)
            {
                var3.putStack(null);
            }
            else
            {
                var3.onSlotChanged();
            }

            if (var4.stackSize == var2.stackSize)
            {
                return null;
            }

            var3.onPickupFromSlot(player, var2);
        }

        return var2;
    }
}
