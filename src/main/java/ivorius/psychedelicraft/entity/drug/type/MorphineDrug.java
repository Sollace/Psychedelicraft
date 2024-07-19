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
public class MorphineDrug extends SimpleDrug {
    public static final DrugAttributeFunctions FUNCTIONS = DrugAttributeFunctions.builder()
            .put(HEART_BEAT_VOLUME, (f, t) -> MathUtils.project(f, 0.4F, 1) + (t * 0.0001F) * 1.2F)
            .put(HEART_BEAT_SPEED, (f, t) -> -f * 0.1F - (t * 0.0001F))
            .put(HAND_TREMBLE_STRENGTH, 0.1F)
            .put(VIEW_TREMBLE_STRENGTH, 0.2F)
            .put(PAIN_SUPPRESSION, 0.5F)
            .build();

    public MorphineDrug(double decSpeed, double decSpeedPlus) {
        super(DrugType.MORPHINE, decSpeed, decSpeedPlus);
    }

    @Override
    protected boolean tickSideEffects(DrugProperties properties, Random random) {
        PlayerEntity entity = properties.asEntity();

        double chance = (getActiveValue() - 0.8F) * 0.051F;

        if (entity.age % 20 == 0 && random.nextFloat() < chance) {
            if (random.nextFloat() < 0.8F) {
                entity.damage(properties.damageOf(PSDamageTypes.STROKE), Integer.MAX_VALUE);
                return true;
            }

            if (random.nextFloat() < 0.5F) {
                entity.damage(properties.damageOf(PSDamageTypes.HEART_ATTACK), Integer.MAX_VALUE);
                return true;
            }
        }

        return super.tickSideEffects(properties, random);
    }
}
