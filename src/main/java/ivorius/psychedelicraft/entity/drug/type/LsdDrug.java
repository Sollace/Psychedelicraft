/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.type;

import ivorius.psychedelicraft.entity.drug.DrugType;
import net.minecraft.util.math.MathHelper;

/**
 * Created by Sollace on Feb 6 2023.
 */
public class LsdDrug extends SimpleDrug {
    public LsdDrug(double decSpeed, double decSpeedPlus) {
        super(DrugType.LSD, decSpeed, decSpeedPlus);
    }

    @Override
    public float handTrembleStrength() {
        return MathHelper.clamp(MathHelper.getLerpProgress((float) getActiveValue(), 0.6F, 1), 0, 1);
    }

    @Override
    public float viewTrembleStrength() {
        return MathHelper.clamp(MathHelper.getLerpProgress((float) getActiveValue(), 0.8F, 1), 0, 1);
    }

    @Override
    public float speedModifier() {
        return 1 + (float) getActiveValue() * 0.1F;
    }

    @Override
    public float digSpeedModifier() {
        return 1 + (float) getActiveValue() * 0.1F;
    }

    @Override
    public float soundVolumeModifier() {
        float strength = (MathHelper.clamp(getTicksActive(), 50, 250) - 50) / 200F;
        return 1 + (float)getActiveValue() * 1.75f * strength;
    }

    @Override
    public float colorHallucinationStrength() {
        return (float) Math.max(0, getActiveValue() - 0.6F) * 2.8F;
    }

    @Override
    public float movementHallucinationStrength() {
        return (float) Math.max(0, getActiveValue() - 0.6F) * 1.9F;
    }

    @Override
    public float bloomHallucinationStrength() {
        return (float) getActiveValue() * 0.12F;
    }

    @Override
    public float superSaturationHallucinationStrength() {
        return (float)getActiveValue() * 0.8F;
    }

    @Override
    public float weightlessness() {
        if (getActiveValue() > 0.6F) {
            return (float) getActiveValue() * 0.8F;
        }
        return (float) getActiveValue() * 0.2F;
    }
}
