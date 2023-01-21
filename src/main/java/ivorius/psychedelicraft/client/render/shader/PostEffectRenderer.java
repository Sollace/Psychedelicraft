package ivorius.psychedelicraft.client.render.shader;

import java.util.*;

import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.client.PsychedelicraftClient;
import net.minecraft.client.gl.PostEffectProcessor;

public class PostEffectRenderer {
    private List<LoadedShader> shaders = new ArrayList<>();

    public void render(float tickDelta) {
        if (PsychedelicraftClient.getConfig().visual.shader2DEnabled) {
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.enableTexture();
            RenderSystem.resetTextureMatrix();
            shaders.forEach(shader -> shader.render(tickDelta));
        }
    }

    public void setupDimensions(int width, int height) {
        shaders.forEach(shader -> shader.setupDimensions(width, height));
    }

    public void onShadersLoaded(List<LoadedShader> shaders) {
        List<LoadedShader> oldShaders = this.shaders;
        this.shaders = shaders;
        oldShaders.forEach(PostEffectProcessor::close);
    }
}
