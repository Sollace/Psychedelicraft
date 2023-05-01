/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.type;

import ivorius.psychedelicraft.PSDamageSources;
import ivorius.psychedelicraft.advancement.PSCriteria;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
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
            PlayerEntity entity = drugProperties.asEntity();
            Random random = entity.getRandom();

            double activeValue = getActiveValue();

            if ((entity.age % 20) == 0) {
                double damageChance = (activeValue - 0.9F) * 2;

                if (entity.age % 20 == 0 && random.nextFloat() < damageChance) {
                    entity.damage(PSDamageSources.ALCOHOL_POISONING, (int) ((activeValue - 0.9f) * 50.0f + 4.0f));
                }
            }

            double motionEffect = Math.min(activeValue, 0.8);

            rotateEntityPitch(entity, MathHelper.sin(entity.age / 600F * (float) Math.PI) / 2F * motionEffect * (random.nextFloat() + 0.5F));
            rotateEntityYaw(entity, MathHelper.cos(entity.age / 500F * (float) Math.PI) / 1.3F * motionEffect * (random.nextFloat() + 0.5F));

            rotateEntityPitch(entity, MathHelper.sin(entity.age / 180F * (float) Math.PI) / 3F * motionEffect * (random.nextFloat() + 0.5F));
            rotateEntityYaw(entity, MathHelper.cos(entity.age / 150F * (float) Math.PI) / 2F * motionEffect * (random.nextFloat() + 0.5F));
        }
    }

    @Override
    public void onWakeUp(DrugProperties drugProperties) {
        double value = getActiveValue();

        if (value > 0) {
            super.onWakeUp(drugProperties);

            PlayerEntity player = drugProperties.asEntity();
            Random random = player.world.random;

            if (random.nextFloat() > (1 - value)) {
                player.animateDamage();
                player.playSound(SoundEvents.ENTITY_PLAYER_HURT, 1, 1);
                drugProperties.addToDrug(DrugType.SLEEP_DEPRIVATION, 0.25F);
                PSCriteria.HANGOVER.trigger(player);
            }
        } else {
            super.onWakeUp(drugProperties);
        }
    }
}
