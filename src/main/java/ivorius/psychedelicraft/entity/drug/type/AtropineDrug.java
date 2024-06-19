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
import net.minecraft.util.math.random.Random;

/**
 * Created by lukas on 01.11.14.
 */
public class AtropineDrug extends SimpleDrug {
    public static final DrugAttributeFunctions FUNCTIONS = DrugAttributeFunctions.builder()
            .put(HEART_BEAT_VOLUME, (f, t) -> MathUtils.inverseLerp(f, 0.4F, 1) + (t * 0.0001F) * 1.2F)
            .put(HEART_BEAT_SPEED, (f, t) -> f * 0.1F + (t * 0.0001F))
            .put(COLOR_HALLUCINATION_STRENGTH, 1.4F)
            .put(MOVEMENT_HALLUCINATION_STRENGTH, 0.7F)
            .put(CONTEXTUAL_HALLUCINATION_STRENGTH, 1.2F)
            .put(HAND_TREMBLE_STRENGTH, 0.3F)
            .put(VIEW_TREMBLE_STRENGTH, 0.4F)
            .put(VIEW_WOBBLYNESS, 0.003F)
            .put(HUNGER_SUPPRESSION, 0.1F)
            .build();

    public AtropineDrug(double decSpeed, double decSpeedPlus) {
        super(DrugType.ATROPINE, decSpeed, decSpeedPlus);
    }

    @Override
    public void update(DrugProperties drugProperties) {
        super.update(drugProperties);

        if (getActiveValue() > 0) {
            PlayerEntity entity = drugProperties.asEntity();
            Random random = entity.getWorld().random;

            if (!entity.getWorld().isClient) {
                double chance = (getActiveValue() - 0.8F) * 0.051F;

                if (entity.age % 20 == 0 && random.nextFloat() < chance) {
                    if (random.nextFloat() < 0.8F) {
                        entity.damage(drugProperties.damageOf(PSDamageTypes.STROKE), Integer.MAX_VALUE);
                    } else if (random.nextFloat() < 0.5F) {
                        entity.damage(drugProperties.damageOf(PSDamageTypes.HEART_ATTACK), Integer.MAX_VALUE);
                    }
                }
            }
        }
    }
}
