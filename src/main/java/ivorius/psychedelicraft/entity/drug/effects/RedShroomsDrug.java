/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.effects;

import ivorius.psychedelicraft.entity.drug.DrugType;

/**
 * Created by lukas on 01.11.14.
 */
public class RedShroomsDrug extends SimpleDrug {
    public RedShroomsDrug(double decSpeed, double decSpeedPlus) {
        super(DrugType.RED_SHROOMS, decSpeed, decSpeedPlus);
    }

    @Override
    public float colorHallucinationStrength() {
        return (float) getActiveValue() * 1.3F;
    }

    @Override
    public float movementHallucinationStrength() {
        return (float) getActiveValue() * 0.7F;
    }

    @Override
    public float contextualHallucinationStrength() {
        return (float) getActiveValue() * 0.2f;
    }

    @Override
    public float viewWobblyness() {
        return (float) getActiveValue() * 0.03f;
    }
}
