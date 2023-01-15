/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.shader.legacy;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.client.render.shader.legacy.program.ShaderBlur;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;

/**
 * Created by lukas on 26.04.14.
 */
public class WrapperBlur extends ShaderWrapper<ShaderBlur> {
    private float screenBackgroundBlur;

    public WrapperBlur(String utils) {
        super(new ShaderBlur(Psychedelicraft.LOGGER),
                getRL("shaderBasic.vert"),
                getRL("shaderBlur.frag"),
                utils
        );
    }

    @Override
    public void setShaderValues(float tickDelta, int ticks, Framebuffer buffer) {
        shaderInstance.vBlur = DrugProperties.of(MinecraftClient.getInstance().player).getDrugValue(DrugType.POWER);
        shaderInstance.hBlur = 0;

        float blur = PsychedelicraftClient.getConfig().visual.pauseMenuBlur * screenBackgroundBlur * screenBackgroundBlur * screenBackgroundBlur;
        shaderInstance.vBlur += blur;
        shaderInstance.hBlur += blur;
    }

    @Override
    public void update() {
        if (MinecraftClient.getInstance().isPaused()) {
            screenBackgroundBlur = Math.min(1, screenBackgroundBlur + 0.25F);
        } else {
            screenBackgroundBlur = Math.max(0, screenBackgroundBlur - 0.25F);
        }
    }
}
