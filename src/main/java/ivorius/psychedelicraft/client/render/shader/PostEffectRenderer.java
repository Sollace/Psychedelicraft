package ivorius.psychedelicraft.client.render.shader;

import java.util.*;

import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.client.PsychedelicraftClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;

public class PostEffectRenderer {
    private List<LoadedShader> shaders = new ArrayList<>();

    public void render(float tickDelta) {
        if (PsychedelicraftClient.getConfig().shader2DEnabled.get()) {
            RenderSystem.enableDepthTest();

            if (shaders.size() == 1) {
                shaders.get(0).render(tickDelta);
            } else {
                shaders.forEach(shader -> shader.render(tickDelta));
            }

            MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
            RenderSystem.disableDepthTest();
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
