/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drugs.effects;

import ivorius.psychedelicraft.entity.drugs.DrugType;

/**
 * Created by lukas on 01.11.14.
 */
public class PeyoteDrug extends SimpleDrug {
    public PeyoteDrug(double decSpeed, double decSpeedPlus) {
        super(DrugType.PEYOTE, decSpeed, decSpeedPlus);
    }

    @Override
    public float colorHallucinationStrength() {
        return (float) getActiveValue() * 0.3F;
    }

    @Override
    public float contextualHallucinationStrength() {
        return (float) getActiveValue() * 0.6F;
    }
}
