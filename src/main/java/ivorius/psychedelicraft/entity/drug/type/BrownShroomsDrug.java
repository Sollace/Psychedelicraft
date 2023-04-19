/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.type;

import ivorius.psychedelicraft.entity.drug.DrugType;

/**
 * Created by lukas on 01.11.14.
 */
public class BrownShroomsDrug extends SimpleDrug {
    public BrownShroomsDrug(double decSpeed, double decSpeedPlus) {
        super(DrugType.BROWN_SHROOMS, decSpeed, decSpeedPlus);
    }

    @Override
    public float colorHallucinationStrength() {
        return (float) getActiveValue() * 0.8F;
    }

    @Override
    public float movementHallucinationStrength() {
        return (float) getActiveValue();
    }

    @Override
    public float contextualHallucinationStrength() {
        return (float) getActiveValue() * 0.35F;
    }

    @Override
    public float viewWobblyness() {
        return (float) getActiveValue() * 0.03F;
    }

    @Override
    public float hungerSuppression() {
        return (float)getActiveValue() * 0.1F;
    }
}
