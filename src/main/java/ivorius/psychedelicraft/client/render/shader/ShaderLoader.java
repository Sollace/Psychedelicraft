package ivorius.psychedelicraft.client.render.shader;

import java.io.IOException;
import java.util.*;
import org.slf4j.Logger;

import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.client.render.DrugRenderer;
import ivorius.psychedelicraft.client.render.GLStateProxy;
import ivorius.psychedelicraft.entity.drug.Drug;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.util.MathUtils;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class ShaderLoader implements SynchronousResourceReloader, IdentifiableResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final ShaderLoader POST_EFFECTS = new ShaderLoader(DrugRenderer.INSTANCE.getPostEffects())
            // Add order = Application order!
            .addShader("heat_distortion", UniformBinding.start()
                    .program("heat_distortion", (setter, tickDelta, screenWidth, screenHeight, pass) -> {
                        float strength = DrugRenderer.INSTANCE.getEnvironmentalEffects().getHeatDistortion();

                        if (strength <= 0) {
                            return;
                        }

                        setter.set("pixelSize", 1F / screenWidth, 1F / screenHeight);
                        setter.set("strength", strength);
                        setter.set("ticks", ShaderContext.ticks() * 0.15f);
                        pass.run();
                    }))
            .addShader("underwater_distortion", UniformBinding.start()
                    .program("heat_distortion", (setter, tickDelta, screenWidth, screenHeight, pass) -> {
                        float strength = DrugRenderer.INSTANCE.getEnvironmentalEffects().getWaterDistortion();

                        if (strength <= 0) {
                            return;
                        }

                        setter.set("pixelSize", 1F / screenWidth, 1F / screenHeight);
                        setter.set("strength", strength);
                        setter.set("ticks", ShaderContext.ticks() * 0.03f);
                        pass.run();
                    }))
            .addShader("simple_effects", UniformBinding.start()
                    .bind((setter, tickDelta, screenWidth, screenHeight, pass) -> {
                        setter.set("ticks", ShaderContext.ticks());
                        pass.run();
                    })
                    .program("simple_effects", (setter, tickDelta, screenWidth, screenHeight, pass) -> {
                        var h = ShaderContext.hallucinations();
                        if (setter.setIfNonZero("quickColorRotation", h.getQuickColorRotation(tickDelta))
                         | setter.setIfNonZero("slowColorRotation", h.getSlowColorRotation(tickDelta))
                         | setter.setIfNonZero("desaturation", h.getDesaturation(tickDelta))
                         | setter.setIfNonZero("colorIntensification", h.getColorIntensification(tickDelta))
                         | setter.setIfNonZero("inversion", h.getColorInversion(tickDelta))
                         | h.getPulseColor(tickDelta)[3] > 0
                         | h.getContrastColorization(tickDelta)[3] > 0) {
                            pass.run();
                        }
                    })
                    .program("simple_effects_depth", (setter, tickDelta, screenWidth, screenHeight, pass) -> {
                        var h = ShaderContext.hallucinations();
                        // var pulses = h.getPulseColor(tickDelta);
                        var worldColorization = h.getContrastColorization(tickDelta);
                        if (h.getQuickColorRotation(tickDelta) > 0
                         | h.getSlowColorRotation(tickDelta) > 0
                         | h.getDesaturation(tickDelta) > 0
                         | h.getColorIntensification(tickDelta) > 0
                         | h.getColorInversion(tickDelta) > 0
                         // | pulses[3] > 0
                         | worldColorization[3] > 0) {
                            //setter.set("pulses", pulses);
                            setter.set("colorSafeMode", GLStateProxy.isColorSafeMode() ? 1 : 0);
                            setter.set("worldColorization", worldColorization);
                            pass.run();
                        }
                    }))
            .addShader("ps_blur", UniformBinding.start()
                    .program("ps_blur", (setter, tickDelta, screenWidth, screenHeight, pass) -> {
                        float[] blur = ShaderContext.hallucinations().getBlur(tickDelta);

                        if (blur[0] > 0 || blur[0] > 0) {
                            setter.set("pixelSize", 1F / screenWidth, 1F / screenHeight);
                            setter.set("hBlur", blur[0]);
                            setter.set("vBlur", blur[1]);
                            setter.set("repeats", MathHelper.ceil(Math.max(blur[0], blur[1])));
                            pass.run();
                        }
                    }))
            .addShader("depth_of_field", UniformBinding.start()
                    .program("depth_of_field", (setter, tickDelta, screenWidth, screenHeight, pass) -> {
                        var config = PsychedelicraftClient.getConfig().visual;

                        if ((config.dofFocalBlurFar <= 0 && config.dofFocalBlurNear <= 0)
                         || (config.dofFocalPointNear <= 0 && config.dofFocalPointFar >= ShaderContext.viewDistace())) {
                            return;
                        }

                        float zNear = 0.05f;
                        float zFar = ShaderContext.viewDistace();

                        float focalPointNear = config.dofFocalPointNear / zFar;
                        float focalPointFar = config.dofFocalPointFar / zFar;
                        float focalBlurFar = config.dofFocalBlurFar;
                        float focalBlurNear = config.dofFocalBlurNear;

                        setter.set("pixelSize", 1.0f / screenWidth, 1.0f / screenHeight);
                        setter.set("focalPointNear", focalPointNear);
                        setter.set("focalPointFar", focalPointFar);

                        float maxDof = Math.max(focalBlurFar, focalBlurNear);

                        for (int n = 0; n < MathHelper.ceil(maxDof); n++) {
                            float curBlurNear = MathHelper.clamp(focalBlurNear - n, 0, 1);
                            float curBlurFar = MathHelper.clamp(focalBlurFar - n, 0, 1);

                            if (curBlurNear > 0.0f || curBlurFar > 0.0f) {
                                setter.set("focalBlurNear", curBlurNear);
                                setter.set("focalBlurFar", curBlurFar);

                                for (int i = 0; i < 2; i++) {
                                    setter.set("vertical", i);
                                    pass.run();
                                }
                            }
                        }

                        setter.set("depthRange", zNear, zFar);
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
                    }))
            .addShader("double_vision", UniformBinding.start()
                    .program("double_vision", (setter, tickDelta, screenWidth, screenHeight, pass) -> {
                        float strength = ShaderContext.modifier(Drug.DOUBLE_VISION);

                        if (strength > 0) {
                            setter.set("totalAlpha", strength);
                            setter.set("distance", MathHelper.sin(ShaderContext.ticks() / 20F) * 0.05f * strength);
                            setter.set("stretch", 1 + strength);
                            pass.run();
                        }
                    }))
            .addShader("ps_blur_noise", UniformBinding.start()
                    .program("ps_blur_noise", (setter, tickDelta, screenWidth, screenHeight, pass) -> {
                        float strength = ShaderContext.drug(DrugType.POWER) * 0.6F;

                        if (strength <= 0) {
                            return;
                        }

                        setter.set("pixelSize", 1F / screenWidth, 1F / screenHeight);
                        setter.set("strength", strength);
                        setter.set("seed", new Random((long) (ShaderContext.ticks() * 1000.0)).nextFloat() * 9 + 1);
                        pass.run();
                    }))
            .addShader("underwater_overlay", UniformBinding.start()
                    .program("distortion_map", (setter, tickDelta, screenWidth, screenHeight, pass) -> {
                        float strength = DrugRenderer.INSTANCE.getEnvironmentalEffects().getWaterScreenDistortion();

                        if (strength > 0) {
                            setter.set("totalAlpha", strength);
                            setter.set("strength", strength * 0.2F);
                            setter.set("texTranslation0", 0, ShaderContext.ticks() * 0.005F);
                            setter.set("texTranslation1", 0.5F, ShaderContext.ticks() * 0.007F);
                            pass.run();
                        }
                    }))
            .addShader("digital", UniformBinding.start()
                    .program("digital_depth", (setter, tickDelta, screenWidth, screenHeight, pass) -> {
                        float digital = ShaderContext.drug(DrugType.ZERO);
                        if (digital <= 0) {
                            return;
                        }

                        float[] maxDownscale = PsychedelicraftClient.getConfig().visual.getDigitalEffectPixelResize();
                        float downscale = MathUtils.mixEaseInOut(0, 0.95F, Math.min(digital * 3, 1));
                        downscale += digital * 0.05f; //Bigger pixels!

                        float textProgress = MathUtils.easeZeroToOne((digital - 0.2F) * 5);
                        float binaryProgress = MathUtils.easeZeroToOne((digital - 0.8F) * 10);

                        setter.set("newResolution",
                                screenWidth * (1 + (maxDownscale[0] - 1) * downscale),
                                screenHeight * (1 + (maxDownscale[1] - 1) * downscale)
                        );
                        setter.set("totalAlpha", 1F);
                        setter.set("textProgress", textProgress + binaryProgress);
                        setter.set("maxColors", digital > 0.4F ? (Math.max(256F / ((digital - 0.4F) * 640 + 1), 2)) : -1); //Step 3, 0.2 is enough for only 2 colors
                        setter.set("saturation", 1 - MathUtils.easeZeroToOne((digital - 0.6F) * 5));
                        setter.set("depthRange", 0.05F, ShaderContext.viewDistace());
                        pass.run();
                    }))
        ;


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
