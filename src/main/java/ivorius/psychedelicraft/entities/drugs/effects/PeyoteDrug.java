/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entities.drugs.effects;

/**
 * Created by lukas on 01.11.14.
 */
public class PeyoteDrug extends SimpleDrug {
    public PeyoteDrug(double decSpeed, double decSpeedPlus) {
        super(decSpeed, decSpeedPlus);
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
