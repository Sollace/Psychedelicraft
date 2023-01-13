/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render;

import net.minecraft.util.math.MathHelper;

import javax.annotation.ParametersAreNonnullByDefault;

import ivorius.psychedelicraft.entity.drugs.Drug;
import ivorius.psychedelicraft.entity.drugs.DrugProperties;

import java.util.Random;

/**
 * Created by lukas on 25.02.14.
 */
@ParametersAreNonnullByDefault
public interface DrugEffectInterpreter {
    static float getSmoothVision(DrugProperties drugProperties) {
        float smoothVision = 0.0f;
        for (Drug drug : drugProperties.getAllDrugs())
            smoothVision += drug.headMotionInertness();
        return  1.0f / (1.0f + smoothVision);
    }

    static float getCameraShiftY(DrugProperties drugProperties, float ticks) {
        float amplitude = 0.0f;

        for (Drug drug : drugProperties.getAllDrugs())
            amplitude += (1.0f - amplitude) * drug.viewTrembleStrength();

        if (amplitude > 0.0f)
            return MathHelper.sin(ticks / 3.0f) * MathHelper.sin(ticks / 3.0f) * amplitude * 0.1f;

        return 0.0f;
    }

    static float getCameraShiftX(DrugProperties drugProperties, float ticks) {
        float amplitude = 0.0f;

        for (Drug drug : drugProperties.getAllDrugs())
            amplitude += (1.0f - amplitude) * drug.viewTrembleStrength();

        if (amplitude > 0.0f)
            return (new Random((long) (ticks * 1000.0f)).nextFloat() - 0.5f) * 0.05f * amplitude;

        return 0.0f;
    }

    static float getHandShiftY(DrugProperties drugProperties, float ticks) {
        return getCameraShiftY(drugProperties, ticks) * 0.3f;
    }

    static float getHandShiftX(DrugProperties drugProperties, float ticks) {
        float amplitude = 0.0f;

        for (Drug drug : drugProperties.getAllDrugs())
            amplitude += (1.0f - amplitude) * drug.handTrembleStrength();

        if (amplitude > 0.0f)
            return (new Random((long) (ticks * 1000.0f)).nextFloat() - 0.5f) * 0.015f * amplitude;

        return 0.0f;
    }
}
