/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.type;

import ivorius.psychedelicraft.entity.drug.DrugAttributeFunctions;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.util.MathUtils;

/**
 * Created by lukas on 01.11.14.
 */
public class CannabisDrug extends SimpleDrug {
    public static final DrugAttributeFunctions FUNCTIONS = DrugAttributeFunctions.builder()
        .put(SPEED, f -> (1 - f) * 0.5F + 0.5F)
        .put(DIG_SPEED, f -> (1 - f) * 0.5F + 0.5F)
        .put(SUPER_SATURATION_HALLUCINATION_STRENGTH, f -> MathUtils.inverseLerp(f, 0, 0.5F) * 0.3F)
        .put(COLOR_HALLUCINATION_STRENGTH, f -> MathUtils.inverseLerp(f * 1.3F, 0.5F, 1) * 0.1F)
        .put(MOVEMENT_HALLUCINATION_STRENGTH, f -> MathUtils.inverseLerp(f * 1.3F, 0.5F, 1) * 0.1F)
        .put(CONTEXTUAL_HALLUCINATION_STRENGTH, f -> MathUtils.inverseLerp(f * 1.3F, 0.5F, 1) * 0.1F)
        .put(HEAD_MOTION_INERTNESS, 8F)
        .put(VIEW_WOBBLYNESS, 0.02F)
        .put(HUNGER_SUPPRESSION, -0.2F)
        .build();

    public CannabisDrug(double decSpeed, double decSpeedPlus) {
        super(DrugType.CANNABIS, decSpeed, decSpeedPlus);
    }

    @Override
    public void update(DrugProperties drugProperties) {
        super.update(drugProperties);

        if (getActiveValue() > 0) {
            drugProperties.asEntity().addExhaustion(0.03F * (float) getActiveValue());
        }
    }
}
