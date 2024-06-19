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
public class PeyoteDrug extends SimpleDrug {
    public static final DrugAttributeFunctions FUNCTIONS = DrugAttributeFunctions.builder()
            .put(COLOR_HALLUCINATION_STRENGTH, 0.3F)
            .put(CONTEXTUAL_HALLUCINATION_STRENGTH, 0.3F)
            .build();

    public PeyoteDrug(double decSpeed, double decSpeedPlus) {
        super(DrugType.PEYOTE, decSpeed, decSpeedPlus);

    }
}
