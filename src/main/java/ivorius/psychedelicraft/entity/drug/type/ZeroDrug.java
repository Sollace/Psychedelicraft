/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.type;

import ivorius.psychedelicraft.entity.drug.DrugType;

/**
 * Created by lukas on 01.11.14.
 */
public class ZeroDrug extends SimpleDrug {
    public ZeroDrug(double decSpeed, double decSpeedPlus) {
        super(DrugType.ZERO, decSpeed, decSpeedPlus);
    }
}
