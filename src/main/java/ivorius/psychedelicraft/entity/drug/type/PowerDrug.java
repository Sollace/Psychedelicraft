/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.type;

import ivorius.psychedelicraft.entity.drug.DrugType;

/**
 * Created by lukas on 01.11.14.
 * Updated by Sollace on 4 Jan 2023
 */
public class PowerDrug extends SimpleDrug {
    public PowerDrug(double decSpeed, double decSpeedPlus) {
        super(DrugType.POWER, decSpeed, decSpeedPlus, true);
    }

    @Override
    public float soundVolumeModifier() {
        return 1 - (float) getActiveValue();
    }

    @Override
    public float desaturationHallucinationStrength() {
        return (float)getActiveValue() * 0.75F;
    }

    @Override
    public float motionBlur() {
        return (float) getActiveValue() * 0.3F;
    }
}
