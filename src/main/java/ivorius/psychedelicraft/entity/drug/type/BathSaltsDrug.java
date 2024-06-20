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
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

/**
 * Created by Sollace on Feb 6 2023.
 */
public class BathSaltsDrug extends SimpleDrug {
    public static final DrugAttributeFunctions FUNCTIONS = DrugAttributeFunctions.builder()
            .put(JUMP_CHANCE, f -> MathUtils.project(f, 0.6F, 1) * 0.03F)
            .put(PUNCH_CHANCE, f -> MathUtils.project(f, 0.5F, 1) * 0.02F)
            .put(COLOR_HALLUCINATION_STRENGTH, 0.8F)
            .put(MOVEMENT_HALLUCINATION_STRENGTH, 1F)
            .put(BLOOM_HALLUCINATION_STRENGTH, 0.12F)
            .put(INVERSION_HALLUCINATION_STRENGTH, f -> MathHelper.clamp(f * f * 5.3F, 0, 1.5F))
            .build();

    public BathSaltsDrug(double decSpeed, double decSpeedPlus) {
        super(DrugType.BATH_SALTS, decSpeed, decSpeedPlus);

    }

    @Override
    protected boolean tickSideEffects(DrugProperties properties, Random random) {
        PlayerEntity entity = properties.asEntity();
        double chance = (getActiveValue() - 0.8F) * 0.051F;

        if (entity.age % 20 == 0 && random.nextFloat() < chance) {
            if (random.nextFloat() < 0.4F) {
                entity.damage(properties.damageOf(PSDamageTypes.STROKE), Integer.MAX_VALUE);
                return true;
            }

            if (random.nextFloat() < 0.5F) {
                entity.damage(properties.damageOf(PSDamageTypes.HEART_FAILURE), Integer.MAX_VALUE);
                return true;
            }

            if (random.nextFloat() < 0.5F) {
                entity.damage(properties.damageOf(PSDamageTypes.RESPIRATORY_FAILURE), Integer.MAX_VALUE);
                return true;
            }

            if (random.nextFloat() < 0.5F) {
                entity.damage(properties.damageOf(PSDamageTypes.KIDNEY_FAILURE), Integer.MAX_VALUE);
                return true;
            }
        }

        return super.tickSideEffects(properties, random);
    }

    @Override
    public void onWakeUp(DrugProperties drugProperties) {
        if (getActiveValue() > 0) {
            Random random = drugProperties.asEntity().getWorld().random;

            if (random.nextFloat() < 0.5) {
                drugProperties.asEntity().damage(
                        drugProperties.damageOf(random.nextFloat() < 0.002 ? PSDamageTypes.KIDNEY_FAILURE : PSDamageTypes.IN_SLEEP),
                        Integer.MAX_VALUE
                );
            } else {
                drugProperties.asEntity().addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 300, 0, false, false, false));
                super.onWakeUp(drugProperties);
            }
        } else {
            super.onWakeUp(drugProperties);
        }
    }
}
