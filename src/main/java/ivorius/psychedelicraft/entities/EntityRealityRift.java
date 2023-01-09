/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entities;

import ivorius.psychedelicraft.ParticleHelper;
import ivorius.psychedelicraft.blocks.PSBlocks;
import ivorius.psychedelicraft.entities.drugs.DrugProperties;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.function.Supplier;

import org.joml.Vector3f;

import com.google.common.base.Suppliers;

/**
 * Created by lukas on 03.03.14.
 */
public class EntityRealityRift extends Entity {
    private static final TrackedData<Float> SIZE = DataTracker.registerData(EntityRealityRift.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> UNSTABILITY = DataTracker.registerData(EntityRealityRift.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Boolean> CLOSING = DataTracker.registerData(EntityRealityRift.class, TrackedDataHandlerRegistry.BOOLEAN);

    public float visualRiftSize;

    public EntityRealityRift(EntityType<EntityRealityRift> type, World par1World)
    {
        super(type, par1World);
        // TODO: (Sollace) Minecraft now supports a render bounding box
        this.ignoreCameraFrustum = true; // Change this when MC supports a render bounding box...
        //this.yOffset = this.height / 2.0F;

        setRiftSize((float)world.random.nextTriangular(0.5F, 0.5F));
    }

    @Override
    protected void initDataTracker() {
        this.getDataTracker().startTracking(SIZE, 0F);
        this.getDataTracker().startTracking(CLOSING, false);
        this.getDataTracker().startTracking(UNSTABILITY, 0F);
    }

    public float getRiftSize() {
        return getDataTracker().get(SIZE);
    }

    public void setRiftSize(float size) {
        getDataTracker().set(SIZE, Math.max(0, size));
    }

    public void addToRift(float size) {
        setRiftSize(getRiftSize() + size);
    }

    public float takeFromRift(float size) {
        if (isCritical()) {
            return 0.2f;
        }

        float riftSize = getRiftSize();
        float newVal = Math.max(riftSize - size, 0.0f);

        setRiftSize(newVal);

        return riftSize - newVal;
    }

    public float getCriticalStatus() {
        return getDataTracker().get(UNSTABILITY);
    }

    public void setCriticalStatus(float status) {
        getDataTracker().set(UNSTABILITY, Math.max(0, status));
    }

    public boolean isRiftClosing() {
        return getDataTracker().get(CLOSING);
    }

    public void setRiftClosing(boolean closing) {
        getDataTracker().set(CLOSING, closing);
    }

    public boolean isCritical() {
        return ((getCriticalStatus() > 0) || (getRiftSize() > 3)) && !isRiftClosing();
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
    public boolean canAvoidTraps() {
        return true;
    }

//    @Override
//    public boolean canBeCollidedWith() // Need this for right click interaction
//    {
//        return !isDead;
//    }

    @Override
    public Text getDisplayName() {
        return super.getDisplayName().copy().formatted(Formatting.OBFUSCATED);
    }

    @Override
    public void tick() {
        super.tick();
        setVelocity(Vec3d.ZERO);

        boolean critical = isCritical();

        if (world.isClient) {
            Vec3d pos = getPos();
            Supplier<Vec3d> particlePositionSupplier = () -> {
                float distance = random.nextFloat() * random.nextFloat();
                return ParticleHelper.apply(pos, x -> x + (random.nextFloat() * 8 - 4) * distance).add(0, getHeight() / 2F, 0);
            };
            ParticleHelper.spawnParticles(world, ParticleTypes.LARGE_SMOKE, particlePositionSupplier, Suppliers.ofInstance(Vec3d.ZERO), random.nextInt(3));
            ParticleHelper.spawnParticles(world, new DustParticleEffect(new Vector3f(1, 0.5F, 0.5F), 1), particlePositionSupplier, Suppliers.ofInstance(new Vec3d(-10, -10, -10)), random.nextInt(2));
            ParticleHelper.spawnParticles(world, ParticleTypes.ENCHANT, Suppliers.ofInstance(pos.add(0, 1 + (getHeight() / 2F), 0)), () -> {
                float distance = random.nextFloat() * random.nextFloat();
                return ParticleHelper.apply(Vec3d.ZERO, x -> x + (random.nextFloat() * 8 - 4) * distance).add(0, getHeight() / 2F, 0);
            }, 1);
        }

        float searchDistance = 5.0f + getCriticalStatus() * 50.0f;
        for (LivingEntity entityLivingBase : world.getEntitiesByClass(LivingEntity.class, getBoundingBox().expand(searchDistance), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR)) {
            double dist = entityLivingBase.distanceTo(this);
            double effect = (searchDistance - dist) * 0.0005 * getRiftSize();

            if (effect > 0.0) {
                DrugProperties.of(entityLivingBase).ifPresentOrElse(drugProperties -> {
                    drugProperties.addToDrug("Zero", effect * 20.0f);
                    drugProperties.addToDrug("Power", effect * 200.0f);
                }, () -> {
                    if (critical) {
                        entityLivingBase.damage(DamageSource.MAGIC, (float) effect * 20.0f);
                    }
                });
            }
        }

        if (critical) {
            float prevS = getCriticalStatus();
            float newS = Math.min(prevS + 0.001f, 1.0f);
            setCriticalStatus(newS);

            float prevDesRange = prevS * 50.0f;
            float newDesRange = newS * 50.0f;

            if (prevDesRange < newDesRange) {
                int desRange = MathHelper.ceil(newDesRange);
                BlockPos center = getBlockPos();
                BlockPos.iterateOutwards(center, desRange, desRange, desRange).forEach(p -> {
                    if (p.isWithinDistance(center, newDesRange) && !p.isWithinDistance(center, prevDesRange) && !world.isAir(p)) {
                        world.setBlockState(p, PSBlocks.GLITCH.getDefaultState());
                    }
                });
            }
        }

        if (isRiftClosing()) {
            setRiftSize(getRiftSize() - 1F / 20F);
        } else if (!critical) {
            setRiftSize(getRiftSize() - 1F / 20F / 20F / 60F);
        }

//        setRiftSize(getRiftSize() - 1.0f / 1.0f / 20.0f / 60.0f);
        visualRiftSize = MathUtils.nearValue(visualRiftSize, getRiftSize(), 0.05f, 0.005f);

        if (!world.isClient) {
            if (getCriticalStatus() >= 0.9f) {
                setRiftClosing(true);
            }

            if (visualRiftSize <= 0.0f && getRiftSize() <= 0.0f) {
                discard();
            }
        }
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound compound) {
        setRiftSize(compound.getFloat("riftSize"));
        setRiftClosing(compound.getBoolean("isRiftClosing"));
        setCriticalStatus(compound.getFloat("criticalStatus"));
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound compound) {
        compound.putFloat("riftSize", getRiftSize());
        compound.putBoolean("isRiftClosing", isRiftClosing());
        compound.putFloat("criticalStatus", getCriticalStatus());
    }

}
