/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.type;

import ivorius.psychedelicraft.entity.drug.DrugType;

/**
 * Created by lukas on 01.11.14.
 */
public class WarmthDrug extends SimpleDrug {
    public WarmthDrug(double decSpeed, double decSpeedPlus) {
        super(DrugType.WARMTH, decSpeed, decSpeedPlus, true);
    }

    @Override
    public float bloomHallucinationStrength() {
        return (float)getActiveValue() * 0.5F;
    }

    @Override
    public float superSaturationHallucinationStrength() {
        return (float)getActiveValue() * 0.1F;
    }
}
