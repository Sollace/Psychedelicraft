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
public class RedShroomsDrug extends SimpleDrug {
    public static final DrugAttributeFunctions FUNCTIONS = DrugAttributeFunctions.builder()
            .put(COLOR_HALLUCINATION_STRENGTH, 1.3F)
            .put(MOVEMENT_HALLUCINATION_STRENGTH, 0.7F)
            .put(CONTEXTUAL_HALLUCINATION_STRENGTH, 0.2F)
            .put(VIEW_WOBBLYNESS, 0.03F)
            .put(HUNGER_SUPPRESSION, 0.1F)
            .build();
    public RedShroomsDrug(double decSpeed, double decSpeedPlus) {
        super(DrugType.RED_SHROOMS, decSpeed, decSpeedPlus);
    }
}
