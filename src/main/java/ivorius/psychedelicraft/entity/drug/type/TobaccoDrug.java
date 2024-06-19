/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.type;

import ivorius.psychedelicraft.entity.drug.DrugAttributeFunctions;
import ivorius.psychedelicraft.entity.drug.DrugType;

/**
 * Created by lukas on 01.11.14.
 */
public class TobaccoDrug extends SimpleDrug {
    public static final DrugAttributeFunctions FUNCTIONS = DrugAttributeFunctions.builder()
                .put(DESATURATION_HALLUCINATION_STRENGTH, 0.2F)
                .build();

    public TobaccoDrug(double decSpeed, double decSpeedPlus) {
        super(DrugType.TOBACCO, decSpeed, decSpeedPlus);
    }
}
