/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.effects;

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
        return (float) getActiveValue() * 0.8f;
    }

    @Override
    public float movementHallucinationStrength() {
        return (float) getActiveValue() * 1.0f;
    }

    @Override
    public float contextualHallucinationStrength() {
        return (float) getActiveValue() * 0.35f;
    }

    @Override
    public float viewWobblyness() {
        return (float) getActiveValue() * 0.03f;
    }
}
