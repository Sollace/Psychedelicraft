/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.type;

import ivorius.psychedelicraft.PSDamageSources;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

/**
 * Created by lukas on 01.11.14.
 */
public class AlcoholDrug extends SimpleDrug {
    public AlcoholDrug(double decSpeed, double decSpeedPlus) {
        super(DrugType.ALCOHOL, decSpeed, decSpeedPlus);
    }

    @Override
    public float viewWobblyness() {
        return (float)getActiveValue() * 0.5F;
    }

    @Override
    public float doubleVision() {
        return MathHelper.clamp(MathHelper.getLerpProgress((float)getActiveValue(), 0.25f, 1), 0, 1);
    }

    @Override
    public float motionBlur() {
        return MathHelper.clamp(MathHelper.getLerpProgress((float)getActiveValue(), 0.5f, 1), 0, 1) * 0.3F;
    }

    @Override
    public void update(DrugProperties drugProperties) {
        super.update(drugProperties);

        if (getActiveValue() > 0) {
            int ticksExisted = drugProperties.age;
            PlayerEntity entity = drugProperties.asEntity();
            Random random = entity.getRandom();

            double activeValue = getActiveValue();

            if ((ticksExisted % 20) == 0) {
                double damageChance = (activeValue - 0.9F) * 2;

                if (ticksExisted % 20 == 0 && random.nextFloat() < damageChance) {
                    entity.damage(PSDamageSources.ALCOHOL_POISONING, (int) ((activeValue - 0.9f) * 50.0f + 4.0f));
                }
            }

            double motionEffect = Math.min(activeValue, 0.8);

//            player.motionX += MathHelper.sin(ticksExisted / 10.0F * (float) Math.PI) / 40.0F * motionEffect * (random.nextFloat() + 0.5F);
//            player.motionZ += MathHelper.cos(ticksExisted / 10.0F * (float) Math.PI) / 40.0F * motionEffect * (random.nextFloat() + 0.5F);
//
//            player.motionX *= (random.nextFloat() - 0.5F) * 2 * motionEffect + 1.0F;
//            player.motionZ *= (random.nextFloat() - 0.5F) * 2 * motionEffect + 1.0F;

            rotateEntityPitch(entity, MathHelper.sin(ticksExisted / 600F * (float) Math.PI) / 2F * motionEffect * (random.nextFloat() + 0.5F));
            rotateEntityYaw(entity, MathHelper.cos(ticksExisted / 500F * (float) Math.PI) / 1.3F * motionEffect * (random.nextFloat() + 0.5F));

            rotateEntityPitch(entity, MathHelper.sin(ticksExisted / 180F * (float) Math.PI) / 3F * motionEffect * (random.nextFloat() + 0.5F));
            rotateEntityYaw(entity, MathHelper.cos(ticksExisted / 150F * (float) Math.PI) / 2F * motionEffect * (random.nextFloat() + 0.5F));
        }
    }

    public static void rotateEntityPitch(Entity entity, double amount) {
        entity.setPitch((float)MathHelper.clamp(entity.getPitch() + amount, -90, 90));
    }

    public static void rotateEntityYaw(Entity entity, double amount) {
        entity.setYaw(entity.getYaw() + (float)amount);
    }
}
