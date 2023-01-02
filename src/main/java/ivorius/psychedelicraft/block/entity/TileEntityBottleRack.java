package ivorius.psychedelicraft.block.entity;

import ivorius.ivtoolkit.blocks.IvTileEntityRotatable;
import ivorius.ivtoolkit.raytracing.IvRaytraceableObject;
import ivorius.ivtoolkit.raytracing.IvRaytracedIntersection;
import ivorius.ivtoolkit.raytracing.IvRaytracerMC;
import ivorius.psychedelicraft.items.ItemBottle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lukas on 16.11.14.
 */
public class TileEntityBottleRack extends IvTileEntityRotatable implements IInventory
{
    public ItemStack[] storedBottles = new ItemStack[9];

    public boolean tryStoringItem(ItemStack stack, Entity entity)
    {
        if (stack != null)
        {
            int slot = getHoveredSlot(entity);

            if (slot >= 0 && isItemValidForSlot(slot, stack) && getStackInSlot(slot) == null)
            {
                if (!worldObj.isRemote)
                {
                    setInventorySlotContents(slot, stack.copy());
                    stack.stackSize--;
                }

                return true;
            }
        }

        return false;
    }

    public boolean pickUpItem(EntityPlayer player)
    {
        int slot = getHoveredSlot(player);

        if (slot >= 0)
        {
            ItemStack stack = getStackInSlot(slot);
            if (stack != null)
            {
                if (!worldObj.isRemote && player.inventory.addItemStackToInventory(stack))
                {
                    setInventorySlotContents(slot, null);
                    player.openContainer.detectAndSendChanges();
                }

                return true;
            }
        }

        return false;
    }

    public int getHoveredSlot(Entity entity)
    {
        List<IvRaytraceableObject> raytraceables = getRaytraceableObjects();
        IvRaytracedIntersection intersection = IvRaytracerMC.getFirstIntersection(raytraceables, entity);

        if (intersection != null)
        {
            String info = (String) intersection.getUserInfo();
            if (info.startsWith("Slot"))
                return Integer.valueOf(info.substring(4));
        }

        return -1;
    }

    public List<IvRaytraceableObject> getRaytraceableObjects()
    {
        List<IvRaytraceableObject> raytraceables = new ArrayList<>();

        double px = 1.0 / 16.0;
        for (int x = 0; x < 3; x++)
            for (int y = 0; y < 3; y++)
                raytraceables.add(getRotatedBox("Slot" + (y * 3 + x), (-7 + x * 5) * px, (3 - y * 5) * px, 0, 4 * px, 4 * px, .4));

        for (int x = 0; x < 4; x++)
            raytraceables.add(getRotatedBox("Frame" + x, (-8 + x * 5) * px, -8 * px, 0, px, 1, .4));

        for (int y = 0; y < 4; y++)
            raytraceables.add(getRotatedBox("Board" + y, -.5, (-8 + y * 5) * px, 0, 1, px, .4));

        return raytraceables;
    }

    @Override
    public int getSizeInventory()
    {
        return storedBottles.length;
    }

    @Override
    public ItemStack getStackInSlot(int slot)
    {
        return storedBottles[slot];
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount)
    {
        if (this.storedBottles[slot] != null)
        {
            ItemStack itemstack;

            if (this.storedBottles[slot].stackSize <= amount)
            {
                itemstack = this.storedBottles[slot];
                this.storedBottles[slot] = null;
                this.markDirty();
                worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
                return itemstack;
            }
            else
            {
                itemstack = this.storedBottles[slot].splitStack(amount);

                if (this.storedBottles[slot].stackSize == 0)
                {
                    this.storedBottles[slot] = null;
                }

                this.markDirty();
                worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
                return itemstack;
            }
        }
        else
        {
            return null;
        }
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot)
    {
        if (this.storedBottles[slot] != null)
        {
            ItemStack itemstack = this.storedBottles[slot];
            this.storedBottles[slot] = null;
            return itemstack;
        }
        else
            return null;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack)
    {
        this.storedBottles[slot] = stack;

        if (stack != null && stack.stackSize > this.getInventoryStackLimit())
            stack.stackSize = this.getInventoryStackLimit();

        this.markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    public String getInventoryName()
    {
        return "container.bottleRack";
    }

    @Override
    public boolean hasCustomInventoryName()
    {
        return false;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 1;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) == this && player.getDistanceSq((double) this.xCoord + 0.5D, (double) this.yCoord + 0.5D, (double) this.zCoord + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory()
    {

    }

    @Override
    public void closeInventory()
    {

    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack)
    {
        return stack.getItem() instanceof ItemBottle;
    }

    @Override
    public void readFromNBT(NBTTagCompound par1nbtTagCompound)
    {
        super.readFromNBT(par1nbtTagCompound);

        readItemDataFromNBT(par1nbtTagCompound);
    }

    @Override
    public void writeToNBT(NBTTagCompound par1nbtTagCompound)
    {
        super.writeToNBT(par1nbtTagCompound);

        writeItemDataToNBT(par1nbtTagCompound);
    }

    public void readItemDataFromNBT(NBTTagCompound compound)
    {
        NBTTagList items = compound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        this.storedBottles = new ItemStack[storedBottles.length];

        for (int i = 0; i < items.tagCount(); ++i)
        {
            NBTTagCompound tag = items.getCompoundTagAt(i);
            int var5 = tag.getByte("Slot") & 255;

            if (var5 >= 0 && var5 < this.storedBottles.length)
            {
                this.storedBottles[var5] = ItemStack.loadItemStackFromNBT(tag);
            }
        }
    }

    public void writeItemDataToNBT(NBTTagCompound compound)
    {
        NBTTagList items = new NBTTagList();

        for (int i = 0; i < storedBottles.length; i++)
        {
            if (this.storedBottles[i] != null)
            {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte) i);
                this.storedBottles[i].writeToNBT(tag);
                items.appendTag(tag);
            }
        }

        compound.setTag("Items", items);
    }
}
