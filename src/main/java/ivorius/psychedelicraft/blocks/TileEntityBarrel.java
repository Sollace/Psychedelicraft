/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.blocks;

import ivorius.psychedelicraft.fluids.Fermentable;
import ivorius.psychedelicraft.fluids.PSFluids;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.Packet;
import net.minecraft.util.math.BlockPos;

import static ivorius.psychedelicraft.fluids.FluidHelper.MILLIBUCKETS_PER_LITER;

public class TileEntityBarrel extends BlockEntity
{
    public static final int BARREL_CAPACITY = MILLIBUCKETS_PER_LITER * 16;

    public int barrelWoodType;

    public int timeFermented;

    public float tapRotation = 0.0f;
    public int timeLeftTapOpen = 0;

    public TileEntityBarrel(BlockPos pos, BlockState state)
    {
        super(PSBlockEntities.BARREL, pos, state);
        tank = new FluidTank(BARREL_CAPACITY);
    }

    @Override
    public void updateEntity()
    {
        FluidStack fluidStack = tank.getFluid();
        if (fluidStack != null && fluidStack.getFluid() instanceof Fermentable)
        {
            Fermentable fluidFermentable = (Fermentable) fluidStack.getFluid();
            int neededFermentationTime = fluidFermentable.getFermentationTime(fluidStack, false);

            if (neededFermentationTime >= 0)
            {
                if (timeFermented >= neededFermentationTime)
                {
                    if (!worldObj.isRemote)
                    {
                        fluidFermentable.ferment(fluidStack, false);
                        timeFermented = 0;

                        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
                        markDirty();
                    }
                }
                else
                    timeFermented++;
            }
        }

        if (timeLeftTapOpen > 0)
        {
            timeLeftTapOpen--;
        }

        if (timeLeftTapOpen > 0 && tapRotation < 3.141f * 0.5f)
        {
            tapRotation += 3.141f * 0.1f;
        }
        if (timeLeftTapOpen == 0 && tapRotation > 0.0f)
        {
            tapRotation -= 3.141f * 0.1f;
        }
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
    {
        int fill = super.fill(from, resource, doFill);

        if (doFill)
        {
            double amountFilled = fill / (double) tank.getFluidAmount();
            timeFermented = MathHelper.floor_double(timeFermented * (1.0 - amountFilled));

            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            markDirty();
        }

        return fill;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
    {
        FluidStack drain = super.drain(from, resource, doDrain);

        if (doDrain)
        {
            if (tank.getFluidAmount() == 0)
                timeFermented = 0;

            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            markDirty();
        }

        return drain;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
    {
        FluidStack drain = super.drain(from, maxDrain, doDrain);

        if (doDrain)
        {
            if (tank.getFluidAmount() == 0)
                timeFermented = 0;

            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            markDirty();
        }

        return drain;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbttagcompound)
    {
        super.writeToNBT(nbttagcompound);

        nbttagcompound.setInteger("barrelWoodType", barrelWoodType);

        nbttagcompound.setInteger("timeFermented", timeFermented);

        nbttagcompound.setInteger("timeLeftTapOpen", timeLeftTapOpen);
        nbttagcompound.setFloat("tapRotation", tapRotation);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound)
    {
        tank.setFluid(null); // Doesn't override if empty >.>
        super.readFromNBT(nbttagcompound);

        if (nbttagcompound.hasKey("barrelType")) // Legacy
        {
            int legacyBarrelType = nbttagcompound.getInteger("barrelType");
//            int legacyTimeFermented = nbttagcompound.getInteger("ticksExisted");
            int legacyContainedItems = nbttagcompound.getInteger("currentContainedItems");
            Fluid containedFluid;

            switch (legacyBarrelType)
            {
                case 1:
                    barrelWoodType = 5;
                    containedFluid = PSFluids.alcRedGrapes;
                    break;
                case 2:
                    barrelWoodType = 1;
                    containedFluid = PSFluids.alcJuniper;
                    break;
                default:
                    barrelWoodType = 0;
                    containedFluid = PSFluids.alcWheatHop;
                    break;
            }

            if (legacyContainedItems > 0)
                fill(ForgeDirection.UP, new FluidStack(containedFluid, legacyContainedItems * 1000), true);
        }
        else
        {
            barrelWoodType = nbttagcompound.getInteger("barrelWoodType");

            timeFermented = nbttagcompound.getInteger("timeFermented");
        }

        timeLeftTapOpen = nbttagcompound.getInteger("timeLeftTapOpen");
        tapRotation = nbttagcompound.getFloat("tapRotation");
    }

    public FluidStack containedFluid()
    {
        return tank.getFluid() != null ? tank.getFluid().copy() : null;
    }

    public int getBlockRotation()
    {
        return getBlockMetadata();
    }

    public float getTapRotation()
    {
        return tapRotation;
    }

    public int getNeededFermentationTime()
    {
        FluidStack fluidStack = tank.getFluid();
        if (fluidStack != null && fluidStack.getFluid() instanceof Fermentable)
        {
            Fermentable fluidFermentable = (Fermentable) fluidStack.getFluid();
            return fluidFermentable.getFermentationTime(fluidStack, false);
        }

        return Fermentable.UNFERMENTABLE;
    }

    public int getRemainingFermentationTimeScaled(int scale)
    {
        int neededFermentationTime = getNeededFermentationTime();
        if (neededFermentationTime >= 0)
            return (neededFermentationTime - timeFermented) * scale / neededFermentationTime;

        return scale;
    }

    public boolean isFermenting()
    {
        return getNeededFermentationTime() >= 0;
    }

    @Override
    public Packet getDescriptionPacket()
    {
        return IvTileEntityHelper.getStandardDescriptionPacket(this);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
    {
        readFromNBT(pkt.func_148857_g());
    }
}
