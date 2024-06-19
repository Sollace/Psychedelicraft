/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.type;

import ivorius.psychedelicraft.PSDamageTypes;
import ivorius.psychedelicraft.entity.drug.Drug;
import ivorius.psychedelicraft.entity.drug.DrugAttributeFunctions;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.util.math.MathHelper;

/**
 * Created by Sollace on Feb 6 2023.
 */
public class LsdDrug extends SimpleDrug {
    public static final DrugAttributeFunctions FUNCTIONS = DrugAttributeFunctions.builder()
            .put(HAND_TREMBLE_STRENGTH, f -> MathUtils.inverseLerp(f, 0.6F, 1))
            .put(VIEW_TREMBLE_STRENGTH, f -> MathUtils.inverseLerp(f, 0.8F, 1))
            .put(SPEED, f -> 1 + f * 0.1F)
            .put(DIG_SPEED, f -> 1 + f * 0.1F)
            .put(SOUND_VOLUME, (f, t) -> 1 + f * 1.75f * ((MathHelper.clamp(t, 50, 250) - 50) / 200F))
            .put(COLOR_HALLUCINATION_STRENGTH, f -> Math.max(0, f - 0.6F) * 2.8F)
            .put(MOVEMENT_HALLUCINATION_STRENGTH, f -> Math.max(0, f - 0.6F) * 1.9F)
            .put(BLOOM_HALLUCINATION_STRENGTH, 0.12F)
            .put(SUPER_SATURATION_HALLUCINATION_STRENGTH, 0.8F)
            .put(HUNGER_SUPPRESSION, 0.2F)
            .put(WEIGHTLESSNESS, f -> f * (f > 0.6 ? 0.8F : 0.2F))
            .build();

    public LsdDrug(DrugType type, double decSpeed, double decSpeedPlus) {
        super(type, decSpeed, decSpeedPlus);
    }

    @Override
    public void update(DrugProperties drugProperties) {
        if (getActiveValue() >= 0.99F) {
            Drug caffiene = drugProperties.getDrug(DrugType.CAFFEINE);
            if (caffiene.getActiveValue() > 0) {
                caffiene.addToDesiredValue(-0.5);
                effect /= 2;
            } else {
                drugProperties.asEntity().damage(drugProperties.damageOf(PSDamageTypes.STROKE), 1);
            }
        }
        super.update(drugProperties);
    }
}
