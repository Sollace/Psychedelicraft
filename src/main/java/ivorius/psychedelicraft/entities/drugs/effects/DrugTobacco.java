/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entities.drugs.effects;

/**
 * Created by lukas on 01.11.14.
 */
public class DrugTobacco extends DrugSimple {
    public DrugTobacco(double decSpeed, double decSpeedPlus) {
        super(decSpeed, decSpeedPlus);
    }

    @Override
    public float desaturationHallucinationStrength() {
        return (float)getActiveValue() * 0.2f;
    }
}
