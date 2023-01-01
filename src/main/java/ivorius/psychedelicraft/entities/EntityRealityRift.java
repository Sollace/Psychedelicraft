/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entities;

import ivorius.ivtoolkit.math.IvMathHelper;
import ivorius.psychedelicraft.blocks.PSBlocks;
import ivorius.psychedelicraft.entities.drugs.DrugProperties;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.List;

/**
 * Created by lukas on 03.03.14.
 */
public class EntityRealityRift extends Entity
{
    public float visualRiftSize;

    public EntityRealityRift(EntityType<EntityRealityRift> type, World par1World)
    {
        super(type, par1World);
        // TODO: (Sollace) Minecraft now supports a render bounding box
        this.ignoreCameraFrustum = true; // Change this when MC supports a render bounding box...
        //this.yOffset = this.height / 2.0F;
    }

    @Override
    protected void entityInit()
    {
        this.dataWatcher.addObject(15, 0.0f);
        this.dataWatcher.addObject(14, (byte) 0);
        this.dataWatcher.addObject(13, 0.0f);

        setRiftSize(worldObj.rand.nextFloat() * 0.5f + 0.5f);
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        motionX = 0.0;
        motionY = 0.0;
        motionZ = 0.0;

        boolean critical = isCritical();

        if (worldObj.isRemote)
        {
            int smokeParticles = rand.nextInt(3);
            for (int i = 0; i < smokeParticles; i++)
            {
                float distance = rand.nextFloat() * rand.nextFloat();

                double xP = (rand.nextFloat() * 8.0 - 4.0) * distance;
                double yP = (rand.nextFloat() * 8.0 - 4.0) * distance;
                double zP = (rand.nextFloat() * 8.0 - 4.0) * distance;

                worldObj.spawnParticle("largesmoke", posX + xP, posY + yP + height / 2, posZ + zP, 0.0, 0.0, 0.0);
            }

            int stationarySmoke = rand.nextInt(2);
            for (int i = 0; i < stationarySmoke; i++)
            {
                float distance = rand.nextFloat() * rand.nextFloat();

                double xP = (rand.nextFloat() * 8.0 - 4.0) * distance;
                double yP = (rand.nextFloat() * 8.0 - 4.0) * distance;
                double zP = (rand.nextFloat() * 8.0 - 4.0) * distance;

                worldObj.spawnParticle("reddust", posX + xP, posY + yP + height / 2, posZ + zP, -10.0, -10.0, -10.0);
            }

            int textParticles = 1;
            for (int i = 0; i < textParticles; i++)
            {
                double xP = rand.nextFloat() * 8.0 - 4.0;
                double yP = rand.nextFloat() * 8.0 - 4.0;
                double zP = rand.nextFloat() * 8.0 - 4.0;

                worldObj.spawnParticle("enchantmenttable", posX, posY + 1.0 + height / 2, posZ, xP, yP, zP);
            }
        }

        float searchDistance = 5.0f + getCriticalStatus() * 50.0f;
        List<EntityLivingBase> entityList = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, boundingBox.expand(searchDistance, searchDistance, searchDistance));
        for (EntityLivingBase entityLivingBase : entityList)
        {
            DrugProperties drugProperties = DrugProperties.getDrugProperties(entityLivingBase);
            double dist = entityLivingBase.getDistanceToEntity(this);

            double effect = (searchDistance - dist) * 0.0005 * getRiftSize();

            if (effect > 0.0)
            {
                if (drugProperties != null)
                {
                    drugProperties.addToDrug("Zero", effect * 20.0f);
                    drugProperties.addToDrug("Power", effect * 200.0f);
                }
                else if (critical)
                {
                    entityLivingBase.attackEntityFrom(DamageSource.magic, (float) effect * 20.0f);
                }
            }
        }

        if (critical)
        {
            float prevS = getCriticalStatus();
            float newS = Math.min(prevS + 0.001f, 1.0f);
            setCriticalStatus(newS);

            float prevDesRange = prevS * 50.0f;
            float newDesRange = newS * 50.0f;
            int desRange = MathHelper.ceiling_float_int(newDesRange);

            if (prevDesRange < newDesRange)
            {
                int nX = MathHelper.floor_double(posX);
                int nY = MathHelper.floor_double(posY);
                int nZ = MathHelper.floor_double(posZ);

                for (int x = -desRange; x <= desRange; x++)
                {
                    for (int y = -desRange; y <= desRange; y++)
                    {
                        for (int z = -desRange; z <= desRange; z++)
                        {
                            float acRange = x * x + y * y + z * z;
                            if (acRange <= newDesRange * newDesRange && acRange > prevDesRange * prevDesRange)
                            {
                                Block b = worldObj.getBlock(nX + x, nY + y, nZ + z);

                                if (b.getBlockHardness(worldObj, nX + x, nY + y, nZ + z) >= 0.0f && b.getMaterial() != Material.air)
                                {
                                    worldObj.setBlock(nX + x, nY + y, nZ + z, PSBlocks.glitched);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isRiftClosing())
        {
            setRiftSize(getRiftSize() - 1.0f / 20.0f);
        }
        else if (!critical)
        {
            setRiftSize(getRiftSize() - 1.0f / 20.0f / 20.0f / 60.0f);
        }

//        setRiftSize(getRiftSize() - 1.0f / 1.0f / 20.0f / 60.0f);

        visualRiftSize = IvMathHelper.nearValue(visualRiftSize, getRiftSize(), 0.05f, 0.005f);

        if (!worldObj.isRemote)
        {
            if (getCriticalStatus() >= 0.9f)
            {
                setRiftClosing(true);
            }

            if (visualRiftSize <= 0.0f && getRiftSize() <= 0.0f)
            {
                setDead();
            }
        }
    }

//    @Override
//    public boolean interactFirst(EntityPlayer par1EntityPlayer)
//    {
//        ItemStack heldItem = par1EntityPlayer.getHeldItem();
//        if (!isRiftClosing() && heldItem.getItem() == Psychedelicraft.itemRiftObtainer && heldItem.getItemDamage() == 0)
//        {
//            heldItem.setItemDamage(1);
//            setRiftClosing(true);
//
//            return true;
//        }
//
//        return super.interactFirst(par1EntityPlayer);
//    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound var1)
    {
        setRiftSize(var1.getFloat("riftSize"));
        setRiftClosing(var1.getBoolean("isRiftClosing"));
        setCriticalStatus(var1.getFloat("criticalStatus"));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound var1)
    {
        var1.setFloat("riftSize", getRiftSize());
        var1.setBoolean("isRiftClosing", isRiftClosing());
        var1.setFloat("criticalStatus", getCriticalStatus());
    }

    public float getRiftSize()
    {
        return dataWatcher.getWatchableObjectFloat(15);
    }

    public void setRiftSize(float size)
    {
        dataWatcher.updateObject(15, size > 0.0f ? size : 0.0f);
    }

    public float getCriticalStatus()
    {
        return dataWatcher.getWatchableObjectFloat(13);
    }

    public void setCriticalStatus(float status)
    {
        dataWatcher.updateObject(13, status > 0.0f ? status : 0.0f);
    }

    public void addToRift(float size)
    {
        setRiftSize(getRiftSize() + size);
    }

    public float takeFromRift(float size)
    {
        if (isCritical())
        {
            return 0.2f;
        }

        float riftSize = getRiftSize();
        float newVal = Math.max(riftSize - size, 0.0f);

        setRiftSize(newVal);

        return riftSize - newVal;
    }

    public boolean isRiftClosing()
    {
        return dataWatcher.getWatchableObjectByte(14) == 1;
    }

    public void setRiftClosing(boolean closing)
    {
        dataWatcher.updateObject(14, closing ? (byte) 1 : (byte) 0);
    }

    public boolean isCritical()
    {
        return ((getCriticalStatus() > 0.0f) || (getRiftSize() > 3.0f)) && !isRiftClosing();
    }

    @Override
    protected boolean canTriggerWalking()
    {
        return false;
    }

//    @Override
//    public boolean canBeCollidedWith() // Need this for right click interaction
//    {
//        return !isDead;
//    }

    @Override
    public boolean shouldRenderInPass(int pass)
    {
        return pass == 1;
    }

    @Override
    public String getCommandSenderName()
    {
        return EnumChatFormatting.OBFUSCATED + super.getCommandSenderName();
    }
}
