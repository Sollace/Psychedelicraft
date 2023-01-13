/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.shader;

import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.client.render.effect.EffectMotionBlur;
import ivorius.psychedelicraft.entity.drugs.DrugProperties;
import net.minecraft.client.MinecraftClient;

/**
 * Created by lukas on 26.04.14.
 */
public class WrapperMotionBlur extends ScreenEffectWrapper<EffectMotionBlur> {
    public WrapperMotionBlur() {
        super(new EffectMotionBlur());
    }

    @Override
    public void setScreenEffectValues(float partialTicks, int ticks) {
        DrugProperties drugProperties = DrugProperties.getDrugProperties(MinecraftClient.getInstance().cameraEntity);

        if (PsychedelicraftClient.getConfig().visual.doMotionBlur && drugProperties != null) {
            screenEffect.motionBlur = drugProperties.hallucinationManager.getMotionBlur(drugProperties, partialTicks);
        } else {
            screenEffect.motionBlur = 0.0f;
        }
    }
}
