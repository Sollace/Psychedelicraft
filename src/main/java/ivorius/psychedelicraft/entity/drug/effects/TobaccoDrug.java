/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.effects;

import ivorius.psychedelicraft.entity.drug.DrugType;

/**
 * Created by lukas on 01.11.14.
 */
public class TobaccoDrug extends SimpleDrug {
    public TobaccoDrug(double decSpeed, double decSpeedPlus) {
        super(DrugType.TOBACCO, decSpeed, decSpeedPlus);
    }

    @Override
    public float desaturationHallucinationStrength() {
        return (float)getActiveValue() * 0.2F;
    }
}
