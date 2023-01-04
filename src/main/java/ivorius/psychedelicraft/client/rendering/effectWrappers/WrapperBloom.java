/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering.effectWrappers;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.rendering.shaders.ShaderBloom;
import ivorius.psychedelicraft.entities.drugs.DrugProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;

/**
 * Created by lukas on 26.04.14.
 */
public class WrapperBloom extends ShaderWrapper<ShaderBloom> {
    public WrapperBloom(String utils) {
        super(new ShaderBloom(Psychedelicraft.logger), getRL("shaderBasic.vert"), getRL("shaderBloom.frag"), utils);
    }

    @Override
    public void setShaderValues(float partialTicks, int ticks, @Nullable Framebuffer depthBuffer) {
        shaderInstance.bloom = DrugProperties.of(MinecraftClient.getInstance().cameraEntity)
                .map(d -> d.hallucinationManager.getBloom(d, partialTicks))
                .orElse(0F);
    }
}
