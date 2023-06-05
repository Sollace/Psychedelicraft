/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.type;

import ivorius.psychedelicraft.PSDamageTypes;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

/**
 * Created by lukas on 01.11.14.
 */
public class AtropineDrug extends SimpleDrug {
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

    @Override
    public float heartbeatVolume() {
        return MathHelper.clamp(MathHelper.getLerpProgress((float) getActiveValue(), 0.4F, 1) + (getTicksActive() * 0.0001F), 0, 1) * 1.2F;
    }

    @Override
    public float heartbeatSpeed() {
        return (float) getActiveValue() * 0.1F + (getTicksActive() * 0.0001F);
    }

    @Override
    public float colorHallucinationStrength() {
        return (float) getActiveValue() * 1.4F;
    }

    @Override
    public float movementHallucinationStrength() {
        return (float) getActiveValue() * 0.7F;
    }

    @Override
    public float contextualHallucinationStrength() {
        return (float) getActiveValue() * 1.2F;
    }

    @Override
    public float handTrembleStrength() {
        return (float) getActiveValue() * 0.3F;
    }

    @Override
    public float viewTrembleStrength() {
        return (float) getActiveValue() * 0.4F;
    }

    @Override
    public float viewWobblyness() {
        return (float) getActiveValue() * 0.003F;
    }

    @Override
    public float hungerSuppression() {
        return (float)getActiveValue() * 0.1F;
    }
}
