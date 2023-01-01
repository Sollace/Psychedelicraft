/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entities.drugs.effects;

/**
 * Created by lukas on 01.11.14.
 */
public class DrugPeyote extends DrugSimple {
    public DrugPeyote(double decSpeed, double decSpeedPlus) {
        super(decSpeed, decSpeedPlus);
    }

    @Override
    public float colorHallucinationStrength() {
        return (float) getActiveValue() * 0.3f;
    }

    @Override
    public float contextualHallucinationStrength() {
        return (float) getActiveValue() * 0.6f;
    }
}
