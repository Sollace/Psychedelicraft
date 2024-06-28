/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.type;

import ivorius.psychedelicraft.entity.drug.DrugAttributeFunctions;
import ivorius.psychedelicraft.entity.drug.DrugType;

/**
 * Created by lukas on 01.11.14.
 * Updated by Sollace on 4 Jan 2023
 */
public class PowerDrug extends SimpleDrug {
    public static final DrugAttributeFunctions FUNCTIONS = DrugAttributeFunctions.builder()
            .put(SOUND_VOLUME, f -> 1 - f)
            .put(DESATURATION_HALLUCINATION_STRENGTH, 0.75F)
            .put(MOTION_BLUR, 0.3F)
            .build();

    public PowerDrug(double decSpeed, double decSpeedPlus) {
        super(DrugType.POWER, decSpeed, decSpeedPlus, true);
    }
}
