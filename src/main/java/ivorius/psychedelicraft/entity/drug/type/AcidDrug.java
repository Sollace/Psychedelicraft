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
public class AcidDrug extends SimpleDrug {
    public AcidDrug(double decSpeed, double decSpeedPlus) {
        super(DrugType.ACID, decSpeed, decSpeedPlus);
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
        return 1 - (float) getActiveValue();
    }

    @Override
    public float motionBlur() {
        return 0;//(float) getActiveValue() * 0.03F;
    }

    @Override
    public float colorHallucinationStrength() {
        return (float) getActiveValue() * 1.8F;
    }

    @Override
    public float movementHallucinationStrength() {
        return (float) getActiveValue() * 0.9F;
    }

    @Override
    public float bloomHallucinationStrength() {
        return (float) getActiveValue() * 0.12F;
    }

    @Override
    public float weightlessness() {
        return (float) getActiveValue() * 0.2F;
    }
}
