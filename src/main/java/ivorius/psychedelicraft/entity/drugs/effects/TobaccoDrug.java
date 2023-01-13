/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drugs.effects;

/**
 * Created by lukas on 01.11.14.
 */
public class TobaccoDrug extends SimpleDrug {
    public TobaccoDrug(double decSpeed, double decSpeedPlus) {
        super(decSpeed, decSpeedPlus);
    }

    @Override
    public float desaturationHallucinationStrength() {
        return (float)getActiveValue() * 0.2F;
    }
}
