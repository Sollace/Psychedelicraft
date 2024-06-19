/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.type;

import ivorius.psychedelicraft.PSDamageTypes;
import ivorius.psychedelicraft.entity.drug.DrugAttributeFunctions;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

/**
 * Created by Sollace on Feb 17 2023.
 *
 * Drowsiness, slurred speech, poor concentration, confusion, dizziness, problems with movement and memory, lowered blood pressure, slowed breathing.
 *
 */
public class KavaDrug extends SimpleDrug {
    public static final DrugAttributeFunctions FUNCTIONS = DrugAttributeFunctions.builder()
            .put(VIEW_WOBBLYNESS, 0.5F)
            .put(DROWSYNESS, 0.4F)
            .put(SOUND_VOLUME, f -> 1 - f)
            .put(SPEED, -0.2F)
            .put(DIG_SPEED, -0.2F)
            .put(DESATURATION_HALLUCINATION_STRENGTH, (f, t) -> f * 0.75f * ((MathHelper.clamp(t, 50, 250) - 50) / 200F))
            .put(HEART_BEAT_VOLUME, (f, t) -> MathUtils.inverseLerp(f, 0.4F, 1) * 1.2F * ((MathHelper.clamp(t, 50, 250) - 50) / 200F))
            .build();

    public KavaDrug(double decSpeed, double decSpeedPlus) {
        super(DrugType.KAVA, decSpeed, decSpeedPlus);
    }

    @Override
    public void update(DrugProperties drugProperties) {
        super.update(drugProperties);

        if (getActiveValue() > 0) {
            PlayerEntity entity = drugProperties.asEntity();
            Random random = entity.getRandom();

            double activeValue = getActiveValue();

            if ((entity.age % 20) == 0 && getTicksActive() > 100) {
                double damageChance = (activeValue - 1.3F) * 2;

                if (entity.age % 10 == 0 && random.nextFloat() < damageChance) {
                    entity.damage(drugProperties.damageOf(PSDamageTypes.HEART_FAILURE), (int) ((activeValue - 0.9f) * 50.0f + 4.0f));
                }
            }

            double motionEffect = Math.min(activeValue, 0.8);

            rotateEntityPitch(entity, MathHelper.sin(entity.age / 600F * (float) Math.PI) / 2F * motionEffect * (random.nextFloat() + 0.5F));
            rotateEntityYaw(entity, MathHelper.cos(entity.age / 500F * (float) Math.PI) / 1.3F * motionEffect * (random.nextFloat() + 0.5F));

            rotateEntityPitch(entity, MathHelper.sin(entity.age / 180F * (float) Math.PI) / 3F * motionEffect * (random.nextFloat() + 0.5F));
            rotateEntityYaw(entity, MathHelper.cos(entity.age / 150F * (float) Math.PI) / 2F * motionEffect * (random.nextFloat() + 0.5F));
        }
    }
}
