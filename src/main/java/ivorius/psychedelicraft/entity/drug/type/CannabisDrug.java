/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.type;

import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import net.minecraft.util.math.MathHelper;

/**
 * Created by lukas on 01.11.14.
 */
public class CannabisDrug extends SimpleDrug {
    public CannabisDrug(double decSpeed, double decSpeedPlus) {
        super(DrugType.CANNABIS, decSpeed, decSpeedPlus);
    }

    @Override
    public void update(DrugProperties drugProperties) {
        super.update(drugProperties);

        if (getActiveValue() > 0) {
            drugProperties.asEntity().addExhaustion(0.03F * (float) getActiveValue());
        }
    }

    @Override
    public float speedModifier() {
        return (1.0F - (float) getActiveValue()) * 0.5F + 0.5F;
    }

    @Override
    public float digSpeedModifier() {
        return (1.0F - (float) getActiveValue()) * 0.5F + 0.5F;
    }

    @Override
    public float superSaturationHallucinationStrength() {
        return MathHelper.clamp(MathHelper.getLerpProgress((float)getActiveValue(), 0, 0.5f), 0, 1) * 0.3F;
    }

    @Override
    public float colorHallucinationStrength() {
        return MathHelper.clamp(MathHelper.getLerpProgress((float) getActiveValue() * 1.3F, 0.5F, 1), 0, 1) * 0.1F;
    }

    @Override
    public float movementHallucinationStrength() {
        return MathHelper.clamp(MathHelper.getLerpProgress((float) getActiveValue() * 1.3F, 0.5f, 1), 0, 1) * 0.1F;
    }

    @Override
    public float contextualHallucinationStrength() {
        return MathHelper.clamp(MathHelper.getLerpProgress((float) getActiveValue() * 1.3F, 0.5f, 1), 0, 1) * 0.1F;
    }

    @Override
    public float headMotionInertness() {
        return (float)getActiveValue() * 8.0F;
    }

    @Override
    public float viewWobblyness() {
        return (float)getActiveValue() * 0.02F;
    }
}
