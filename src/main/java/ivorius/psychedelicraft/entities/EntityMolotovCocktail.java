/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entities;

import ivorius.psychedelicraft.fluids.ExplodingFluid;
import ivorius.psychedelicraft.items.ItemMolotovCocktail;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;

public class EntityMolotovCocktail extends ThrownItemEntity
{
    public ItemStack molotovStack;

    public EntityMolotovCocktail(EntityType<EntityMolotovCocktail> type, World world) {
        super(type, world);
    }

    public EntityMolotovCocktail(World par1World, EntityLivingBase par2EntityLiving)
    {
        super(par1World, par2EntityLiving);
    }

    public EntityMolotovCocktail(World par1World, double par2, double par4, double par6)
    {
        super(par1World, par2, par4, par6);
    }

    @Override
    public void onEntityUpdate()
    {
        super.onEntityUpdate();

        worldObj.spawnParticle("flame", posX, posY, posZ, 0.0D, 0.0D, 0.0D);
        worldObj.spawnParticle("flame", posX + motionX / 2, posY + motionY / 2, posZ + motionZ / 2, 0.0D, 0.0D, 0.0D);
    }

    @Override
    protected void onImpact(MovingObjectPosition par1MovingObjectPosition)
    {
        worldObj.playSoundAtEntity(this, "game.potion.smash", 1.0F, 1.0F);

        float explosionStrength = 0.0f;
        float fireStrength = 0.0f;

        FluidStack explodingFluidStack = molotovStack != null ? ItemMolotovCocktail.getExplodingFluid(molotovStack) : null;
        if (explodingFluidStack != null)
        {
            ExplodingFluid explodingFluid = (ExplodingFluid) explodingFluidStack.getFluid();
            explosionStrength = explodingFluid.explosionStrength(explodingFluidStack);
            fireStrength = explodingFluid.fireStrength(explodingFluidStack);
        }

        if (par1MovingObjectPosition.entityHit != null)
        {
            if (par1MovingObjectPosition.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, getThrower()), 4))
            {
                // TODO Implement hit damage
            }
        }

        // onImpact not called on client
//        for (int i = 0; i < fireStrength * 2; i++)
//        {
//            worldObj.spawnParticle("flame", posX + (rand.nextDouble() - 0.5D) * fireStrength, posY + (rand.nextDouble() - 0.5D) * fireStrength, posZ + (rand.nextDouble() - 0.5D) * fireStrength, 0.0D, 0.0D, 0.0D);
//
//            worldObj.spawnParticle("lava", posX, posY + height, posZ, 0.0D, 0.0D, 0.0D);
//        }

        if (!worldObj.isRemote)
        {
            if (explosionStrength > 0)
            {
                worldObj.createExplosion(this, posX, posY, posZ, explosionStrength, false);
            }

            int rangeInt = MathHelper.ceiling_float_int(fireStrength);
            int refX = MathHelper.floor_double(posX);
            int refY = MathHelper.floor_double(posY);
            int refZ = MathHelper.floor_double(posZ);

            for (int x = -rangeInt; x <= rangeInt; x++)
                for (int y = -rangeInt; y <= rangeInt; y++)
                    for (int z = -rangeInt; z <= rangeInt; z++)
                    {
                        if (x * x + y * y + z * z < fireStrength * fireStrength)
                        {
                            if (worldObj.getBlock(refX + x, refY + y, refZ + z).isReplaceable(worldObj, refX + x, refY + y, refZ + z)
                                    && Blocks.fire.canPlaceBlockAt(worldObj, refX + x, refY + y, refZ + z) && rand.nextInt(2) == 0)
                            {
                                worldObj.setBlock(refX + x, refY + y, refZ + z, Blocks.fire, 0, 3);
                            }
                        }
                    }
        }

        setDead();
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);

        if (nbt.hasKey("molotovStack", Constants.NBT.TAG_COMPOUND))
            molotovStack = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("molotovStack"));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);

        if (molotovStack != null)
        {
            NBTTagCompound stackNBT = new NBTTagCompound();
            molotovStack.writeToNBT(stackNBT);
            nbt.setTag("molotovStack", stackNBT);
        }
    }
}
