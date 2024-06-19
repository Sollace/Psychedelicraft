/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.type;

import ivorius.psychedelicraft.entity.drug.Drug;
import ivorius.psychedelicraft.entity.drug.DrugAttributeFunctions;
import ivorius.psychedelicraft.entity.drug.DrugType;

/**
 * Created by lukas on 01.11.14.
 */
public class WarmthDrug extends SimpleDrug {
    public static final DrugAttributeFunctions FUNCTIONS = DrugAttributeFunctions.builder()
            .put(Drug.BLOOM_HALLUCINATION_STRENGTH, 0.5F)
            .put(Drug.SUPER_SATURATION_HALLUCINATION_STRENGTH, 0.1F)
            .build();

    public WarmthDrug(double decSpeed, double decSpeedPlus) {
        super(DrugType.WARMTH, decSpeed, decSpeedPlus, true);
    }
}
