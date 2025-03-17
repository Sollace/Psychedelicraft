package ivorius.psychedelicraft.client.render.shader;

import java.util.*;

import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.client.PsychedelicraftClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Pool;

public class PostEffectRenderer {
    private List<LoadedShader> shaders = new ArrayList<>();

    public void render(Pool pool, float tickDelta) {
        if (PsychedelicraftClient.getConfig().shader2DEnabled.get()) {
            RenderSystem.enableDepthTest();

            if (shaders.size() == 1) {
                shaders.get(0).render(pool, tickDelta);
            } else {
                shaders.forEach(shader -> shader.render(pool, tickDelta));
            }

            MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
            RenderSystem.disableDepthTest();
        }
    }

    public void onShadersLoaded(List<LoadedShader> shaders) {
        this.shaders = shaders;
    }
}
