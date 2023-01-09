/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering.effectWrappers;

import ivorius.psychedelicraft.client.ClientProxy;
import ivorius.psychedelicraft.client.rendering.EffectMotionBlur;
import ivorius.psychedelicraft.config.PSConfig;
import ivorius.psychedelicraft.entities.drugs.DrugProperties;
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

        if (PSConfig.<ClientProxy.Config>getInstance().visual.doMotionBlur && drugProperties != null) {
            screenEffect.motionBlur = drugProperties.hallucinationManager.getMotionBlur(drugProperties, partialTicks);
        } else {
            screenEffect.motionBlur = 0.0f;
        }
    }
}
