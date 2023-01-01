/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.blocks;

import io.netty.buffer.ByteBuf;
import ivorius.ivtoolkit.bezier.IvBezierPath3D;
import ivorius.ivtoolkit.blocks.IvTileEntityHelper;
import ivorius.ivtoolkit.math.IvMathHelper;
import ivorius.ivtoolkit.network.IvNetworkHelperServer;
import ivorius.ivtoolkit.network.PartialUpdateHandler;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.entities.EntityRealityRift;
import ivorius.psychedelicraft.entities.drugs.DrugProperties;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TileEntityRiftJar extends BlockEntity implements PartialUpdateHandler
{
    public float currentRiftFraction;
    public int ticksAliveVisual;

    public boolean isOpening;
    public float fractionOpen;

    public boolean jarBroken = false;
    public boolean suckingRifts = true;
    public float fractionHandleUp;

    public ArrayList<JarRiftConnection> riftConnections = new ArrayList<>();

    @Override
    public void updateEntity()
    {
        fractionOpen = IvMathHelper.nearValue(fractionOpen, isOpening ? 1.0f : 0.0f, 0.0f, 0.02f);
        fractionHandleUp = IvMathHelper.nearValue(fractionHandleUp, isSuckingRifts() ? 0.0f : 1.0f, 0.0f, 0.04f);

//        if (!worldObj.isRemote)
//        {
//            boolean before = suckingRifts;
//            suckingRifts = !worldObj.isDaytime() && worldObj.canBlockSeeTheSky(xCoord, yCoord, zCoord);
//
//            if (before != suckingRifts)
//            {
//                markDirty();
//                worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
//            }
//        }

        if (isSuckingRifts())
        {
            if (fractionOpen > 0.0f)
            {
                List<EntityRealityRift> rifts = getAffectedRifts();

                if (rifts.size() > 0)
                {
                    float minus = (1.0f / rifts.size()) * 0.001f * fractionOpen;
                    for (EntityRealityRift rift : rifts)
                    {
                        currentRiftFraction += rift.takeFromRift(minus);

                        JarRiftConnection connection = createAndGetRiftConnection(rift);
                        connection.fractionUp += 0.02f * fractionOpen;
                        if (connection.fractionUp > 1.0f)
                        {
                            connection.fractionUp = 1.0f;
                        }
                    }
                }
            }
        }
        else
        {
            if (fractionOpen > 0.0f)
            {
                float minus = Math.min(0.0004f * fractionOpen * currentRiftFraction + 0.0004f, currentRiftFraction);

                List<EntityLivingBase> entities = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, AxisAlignedBB.getBoundingBox(xCoord - 5.0f, yCoord - 5.0f, zCoord - 2.0f, xCoord + 6.0f, yCoord + 6.0f, zCoord + 6.0f));

                for (EntityLivingBase entity : entities)
                {
                    double effect = (5.0f - entity.getDistance(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5)) * 0.2f * minus;
                    DrugProperties drugProperties = DrugProperties.getDrugProperties(entity);

                    if (drugProperties != null)
                    {
                        drugProperties.addToDrug("Zero", effect * 5.0f);
                        drugProperties.addToDrug("Power", effect * 35.0f);
                    }
                }

                currentRiftFraction -= minus;
            }
        }

        Iterator<JarRiftConnection> jarRiftConnectionIterator = riftConnections.iterator();
        while (jarRiftConnectionIterator.hasNext())
        {
            JarRiftConnection connection = jarRiftConnectionIterator.next();
            connection.fractionUp -= 0.01f;

            if (connection.fractionUp <= 0.0f)
            {
                jarRiftConnectionIterator.remove();
            }
        }

        if (currentRiftFraction > 1.0f)
        {
            jarBroken = true;

            releaseRift();
            worldObj.setBlock(xCoord, yCoord, zCoord, Blocks.air);
            worldObj.createExplosion(null, xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, 1.0f, false);
        }

        ticksAliveVisual++;
    }

    public JarRiftConnection createAndGetRiftConnection(EntityRealityRift rift)
    {
        for (JarRiftConnection connection : riftConnections)
        {
            if (connection.riftID == rift.getEntityId())
            {
                return connection;
            }
        }

        JarRiftConnection newConnection = new JarRiftConnection();
        newConnection.riftID = rift.getEntityId();
        newConnection.fractionUp = 0.0f;
        newConnection.entityX = rift.posX;
        newConnection.entityY = rift.posY + rift.height * 0.5;
        newConnection.entityZ = rift.posZ;
        riftConnections.add(newConnection);

        return newConnection;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbttagcompound)
    {
        super.writeToNBT(nbttagcompound);

        nbttagcompound.setFloat("currentRiftFraction", currentRiftFraction);

        nbttagcompound.setBoolean("isOpening", isOpening);
        nbttagcompound.setFloat("fractionOpen", fractionOpen);

        nbttagcompound.setBoolean("jarBroken", jarBroken);
        nbttagcompound.setBoolean("suckingRifts", suckingRifts);
        nbttagcompound.setFloat("fractionHandleUp", fractionHandleUp);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound)
    {
        super.readFromNBT(nbttagcompound);

        currentRiftFraction = nbttagcompound.getFloat("currentRiftFraction");

        isOpening = nbttagcompound.getBoolean("isOpening");
        fractionOpen = nbttagcompound.getFloat("fractionOpen");

        jarBroken = nbttagcompound.getBoolean("jarBroken");
        suckingRifts = nbttagcompound.getBoolean("suckingRifts");
        fractionHandleUp = nbttagcompound.getFloat("fractionHandleUp");
    }

    public int getBlockRotation()
    {
        return getBlockMetadata();
    }

    public void toggleRiftJarOpen()
    {
        if (!worldObj.isRemote)
        {
            isOpening = !isOpening;

            markDirty();
            IvNetworkHelperServer.sendTileEntityUpdatePacket(this, "isOpening", Psychedelicraft.network);
        }
    }

    public void toggleSuckingRifts()
    {
        if (!worldObj.isRemote)
        {
            suckingRifts = !suckingRifts;

            markDirty();
            IvNetworkHelperServer.sendTileEntityUpdatePacket(this, "suckingRifts", Psychedelicraft.network);
        }
    }


    public boolean isSuckingRifts()
    {
        return suckingRifts;
    }

    public void releaseRift()
    {
        if (currentRiftFraction > 0.0f)
        {
            List<EntityRealityRift> rifts = getAffectedRifts();

            if (rifts.size() > 0)
            {
                rifts.get(0).addToRift(currentRiftFraction);
            }
            else
            {
                if (!worldObj.isRemote)
                {
                    EntityRealityRift rift = new EntityRealityRift(worldObj);
                    rift.setPosition(xCoord + 0.5f, yCoord + 3.0f, zCoord + 0.5f);
                    rift.setRiftSize(currentRiftFraction);
                    worldObj.spawnEntityInWorld(rift);
                }
            }

            currentRiftFraction = 0.0f;
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return AxisAlignedBB.getBoundingBox(xCoord - 10.0f, yCoord - 5.0f, zCoord - 10.0f, xCoord + 11.0f, yCoord + 11.0f, zCoord + 11.0f);
    }

    public List<EntityRealityRift> getAffectedRifts()
    {
        return worldObj.getEntitiesWithinAABB(EntityRealityRift.class, AxisAlignedBB.getBoundingBox(xCoord - 2.0f, yCoord + 0.0f, zCoord - 2.0f, xCoord + 3.0f, yCoord + 10.0f, zCoord + 3.0f));
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

    @Override
    public void writeUpdateData(ByteBuf buffer, String context, Object... params)
    {
        if ("isOpening".equals(context))
        {
            buffer.writeBoolean(isOpening);
        }
        else if ("suckingRifts".equals(context))
        {
            buffer.writeBoolean(suckingRifts);
        }
    }

    @Override
    public void readUpdateData(ByteBuf buffer, String context)
    {
        if ("isOpening".equals(context))
        {
            isOpening = buffer.readBoolean();
        }
        else if ("suckingRifts".equals(context))
        {
            suckingRifts = buffer.readBoolean();
        }
    }

    public static class JarRiftConnection
    {
        public int riftID;
        public double entityX;
        public double entityY;
        public double entityZ;

        public IvBezierPath3D bezierPath3D;
        public float fractionUp;
    }
}
