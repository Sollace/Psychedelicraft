package ivorius.psychedelicraft.client.render.shader;

import java.io.IOException;
import java.util.*;

import org.slf4j.Logger;

import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.render.DrugRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;

public class ShaderLoader implements SynchronousResourceReloader {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final ShaderLoader INSTANCE = new ShaderLoader(DrugRenderer.INSTANCE.getPostEffects())
            // Add order = Application order!
            .addShader("heat_distortion")
            .addShader("simple_effects")
            .addShader("blur")
            .addShader("depth_of_field")
            .addShader("bloom")
            .addShader("colored_bloom")
            .addShader("double_vision")
            .addShader("blur_noise")
            .addShader("underwater_overlay")
            .addShader("digital");

    private final MinecraftClient client = MinecraftClient.getInstance();

    public static final Identifier ID = Psychedelicraft.id("post_effect_shaders");

    private final Set<Identifier> activeShaderIds = new HashSet<>();

    private final PostEffectRenderer renderer;

    public ShaderLoader(PostEffectRenderer renderer) {
        this.renderer = renderer;
    }

    public ShaderLoader addShader(Identifier id) {
        activeShaderIds.add(id);
        return this;
    }

    public ShaderLoader addShader(String name) {
        return addShader(Psychedelicraft.id(name));
    }

    @Override
    public void reload(ResourceManager manager) {
        renderer.onShadersLoaded(activeShaderIds.stream().map(id -> {
            try {
                return new LoadedShader(client, id);
            } catch (IOException e) {
                LOGGER.warn("Failed to load shader: {}", id, e);
            } catch (JsonSyntaxException e) {
                LOGGER.warn("Failed to parse shader: {}", id, e);
            }
            return null;
        }).filter(Objects::nonNull).toList());
    }
}
