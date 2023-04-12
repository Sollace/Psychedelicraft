/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render;

import net.minecraft.util.math.MathHelper;

import ivorius.psychedelicraft.entity.drug.Drug;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;

/**
 * Created by lukas on 25.02.14.
 */
public interface DrugEffectInterpreter {
    static float getCameraShiftY(DrugProperties drugProperties, float ticks) {
        float amplitude = drugProperties.getModifier(Drug.VIEW_TREMBLE_STRENGTH);
        if (amplitude > 0) {
            return MathHelper.square(MathHelper.sin(ticks / 3F)) * amplitude * 0.1F;
        }
        return 0;
    }

    static float getCameraShiftX(DrugProperties drugProperties, float ticks) {
        float amplitude = drugProperties.getModifier(Drug.VIEW_TREMBLE_STRENGTH);
        if (amplitude <= 0) {
            return 0;
        }
        return (RenderUtil.random((long)(ticks * 1000)).nextFloat() - 0.5F) * 0.05F * amplitude;
    }

    static float getHandShiftY(DrugProperties drugProperties, float ticks) {
        return getCameraShiftY(drugProperties, ticks) * 0.3f;
    }

    static float getHandShiftX(DrugProperties drugProperties, float ticks) {
        float amplitude = drugProperties.getModifier(Drug.HAND_TREMBLE_STRENGTH);
        if (amplitude <= 0) {
            return 0;
        }
        return (RenderUtil.random((long)(ticks * 1000)).nextFloat() - 0.5f) * 0.015f * amplitude;
    }

    static float getAlcohol(DrugProperties properties) {
        return MathHelper.clamp(properties.getDrugValue(DrugType.ALCOHOL) + properties.getDrugValue(DrugType.KAVA), 0, 1);
    }

    static boolean getOOB(DrugProperties properties) {
        return properties.getHallucinations().getCamera().getPosition().length() > 0.5F;
    }
}
