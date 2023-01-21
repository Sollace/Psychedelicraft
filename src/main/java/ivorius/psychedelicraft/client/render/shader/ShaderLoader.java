package ivorius.psychedelicraft.client.render.shader;

import java.io.IOException;
import java.util.*;
import org.slf4j.Logger;

import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.render.DrugRenderer;
import ivorius.psychedelicraft.entity.drug.DrugType;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class ShaderLoader implements SynchronousResourceReloader, IdentifiableResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final ShaderLoader POST_EFFECTS = new ShaderLoader(DrugRenderer.INSTANCE.getPostEffects())
        .addShader("simple_effects", UniformBinding.start()
                .program("simple_effects", (setter, tickDelta, screenWidth, screenHeight, pass) -> {
                    var h = ShaderContext.hallucinations();
                    if (setter.setIfNonZero("quickColorRotation", h.getQuickColorRotation(tickDelta))
                     || setter.setIfNonZero("slowColorRotation", h.getSlowColorRotation(tickDelta))
                     || setter.setIfNonZero("desaturation", h.getDesaturation(tickDelta))
                     || setter.setIfNonZero("colorIntensification", h.getColorIntensification(tickDelta))) {
                        setter.set("ticks", MinecraftClient.getInstance().player.age + tickDelta);
                        pass.run();
                    }
                }))
        .addShader("ps_blur", UniformBinding.start()
                .program("ps_blur", (setter, tickDelta, screenWidth, screenHeight, pass) -> {
                    float menuBlur = DrugRenderer.INSTANCE.getMenuBlur();
                    float vBlur = ShaderContext.drug(DrugType.POWER) + menuBlur;
                    float hBlur = menuBlur;
                    if (vBlur <= 0 && hBlur <= 0) {
                        return;
                    }

                    setter.set("pixelSize", 1F / screenWidth, 1F / screenHeight);
                    setter.set("hBlur", hBlur);
                    setter.set("vBlur", vBlur);
                    setter.set("repeats", MathHelper.ceil(Math.max(hBlur, vBlur)));
                    pass.run();
                }))
        .addShader("ps_bloom", UniformBinding.start()
                .program("ps_bloom", (setter, tickDelta, screenWidth, screenHeight, pass) -> {
                    float bloom = ShaderContext.hallucinations().getBloom(tickDelta);
                    setter.set("pixelSize", 1F / screenWidth * 2F, 1F / screenHeight * 2F);
                    for (int n = 0; n < MathHelper.ceil(bloom); n++) {
                        setter.set("totalAlpha", Math.min(1, bloom - n));
                        for (int i = 0; i < 2; i++) {
                            setter.set("vertical", i);
                            pass.run();
                        }
                    }
                }))
        .addShader("ps_colored_bloom", UniformBinding.start()
                .program("ps_colored_bloom", (setter, tickDelta, screenWidth, screenHeight, pass) -> {
                    float[] color = ShaderContext.hallucinations().getColorBloom(tickDelta);
                    if (color[3] <= 0) {
                        return;
                    }

                    setter.set("bloomColor", color[0], color[1], color[2]);
                    setter.set("pixelSize", 1F / screenWidth, 1F / screenHeight);

                    for (int n = 0; n < MathHelper.ceil(color[3]); n++) {
                        setter.set("totalAlpha", Math.max(1, color[3] - n));
                        for (int i = 0; i < 2; i++) {
                            setter.set("vertical", i);
                            pass.run();
                        }
                    }
                })
        );

    private final MinecraftClient client = MinecraftClient.getInstance();

    private static final Identifier ID = Psychedelicraft.id("post_effect_shaders");

    private final Map<Identifier, UniformBinding.Set> activeShaderIds = new HashMap<>();

    private final PostEffectRenderer renderer;

    public ShaderLoader(PostEffectRenderer renderer) {
        this.renderer = renderer;
    }

    public ShaderLoader addShader(Identifier id, UniformBinding.Set bindings) {
        activeShaderIds.put(id, bindings);
        return this;
    }

    public ShaderLoader addShader(String id, UniformBinding.Set bindings) {
        return addShader(Psychedelicraft.id("shaders/post/" + id + ".json"), bindings);
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        renderer.onShadersLoaded(List.of());
        renderer.onShadersLoaded(activeShaderIds.entrySet().stream().map(this::loadShader).filter(Objects::nonNull).toList());
    }

    public LoadedShader loadShader(Map.Entry<Identifier, UniformBinding.Set> entry) {
        try {
            return new LoadedShader(client, entry.getKey(), entry.getValue());
        } catch (IOException e) {
            LOGGER.warn("Failed to load shader: {}", entry.getKey(), e);
        } catch (JsonSyntaxException e) {
            LOGGER.warn("Failed to parse shader: {}", entry.getKey(), e);
        }
        return null;
    }
}
