package ivorius.psychedelicraft.client.render.shader;

import java.util.HashMap;
import java.util.Map;

import org.joml.Vector4fc;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.client.render.DrugRenderer;
import ivorius.psychedelicraft.client.render.GLStateProxy;
import ivorius.psychedelicraft.client.render.RenderPhase;
import ivorius.psychedelicraft.client.render.RenderUtil;
import ivorius.psychedelicraft.entity.drug.Drug;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public interface UniformBindings {
    Map<Identifier, UniformBinding> VALUES = new HashMap<>();

    static UniformBinding register(Identifier fragmentShaderId, UniformBinding binding) {
        VALUES.put(fragmentShaderId, binding);
        return binding;
    }

    UniformBinding HEAT_DISTORTION_CONFIG = register(Psychedelicraft.id("post/heat_distortion"), (tickDelta, screenWidth, screenHeight, passes) -> {
        float water = DrugRenderer.INSTANCE.getEnvironmentalEffects().getWaterDistortion();
        float strength = Math.max(
                DrugRenderer.INSTANCE.getEnvironmentalEffects().getHeatDistortion(),
                water
        );

        if (strength > 0) {
            float ticks = ShaderContext.ticks() * (water > 0 ? 0.03F : 0.15F);
            float totalAlpha = 1F;

            passes.accept(uniforms -> {
                uniforms.set("DistortionConfig", builder -> builder.putFloat(ticks).putFloat(strength).putFloat(totalAlpha));
            });
        }
    });
    UniformBinding COLOR_ROTATION_CONFIG = register(Psychedelicraft.id("post/simple_effects"), (tickDelta, screenWidth, screenHeight, passes) -> {
        var h = ShaderContext.hallucinations();
        float quickColorRotation = h.get(Drug.FAST_COLOR_ROTATION);
        float slowColorRotation = h.get(Drug.SLOW_COLOR_ROTATION);
        float desaturation = h.get(Drug.DESATURATION_HALLUCINATION_STRENGTH);
        float colorIntensification = h.get(Drug.SUPER_SATURATION_HALLUCINATION_STRENGTH);
        float inversion = h.get(Drug.INVERSION_HALLUCINATION_STRENGTH);
        if (quickColorRotation > 0 || slowColorRotation > 0 || desaturation > 0 || colorIntensification > 0 || inversion > 0
             || h.getPulseColor(tickDelta, RenderPhase.current() == RenderPhase.SKY).w() > 0
             || h.getContrastColorization(tickDelta).w() > 0) {
            passes.accept(uniforms -> {
                uniforms.set("ColorRotationConfig", builder -> builder
                        .putFloat(ShaderContext.ticks())
                        .putFloat(slowColorRotation)
                        .putFloat(quickColorRotation)
                        .putFloat(colorIntensification)
                        .putFloat(desaturation)
                        .putFloat(inversion));
            });
        }
    });
    UniformBinding WORLD_COLORIZATION_CONFIG = register(Psychedelicraft.id("post/simple_effects_depth"), (tickDelta, screenWidth, screenHeight, passes) -> {
        var h = ShaderContext.hallucinations();
        // var pulses = h.getPulseColor(tickDelta);
        var worldColorization = h.getContrastColorization(tickDelta);
        if (h.get(Drug.FAST_COLOR_ROTATION) > 0
         | h.get(Drug.SLOW_COLOR_ROTATION) > 0
         | h.get(Drug.DESATURATION_HALLUCINATION_STRENGTH) > 0
         | h.get(Drug.SUPER_SATURATION_HALLUCINATION_STRENGTH) > 0
         | h.get(Drug.INVERSION_HALLUCINATION_STRENGTH) > 0
         // | pulses[3] > 0
         | worldColorization.w() > 0) {
            passes.accept(uniforms -> {
                uniforms.set("WorldColorizationConfig", builder -> builder
                        .putFloat(ShaderContext.ticks())
                        .putInt(GLStateProxy.isColorSafeMode() ? 1 : 0)
                        //.putVec4(pulses)
                        .putVec4(worldColorization));
            });
        }
    });
    UniformBinding BLUR_CONFIG = register(Psychedelicraft.id("post/blur"), (tickDelta, screenWidth, screenHeight, passes) -> {
        float[] blur = ShaderContext.hallucinations().getBlur();

        if (blur[0] > 0 || blur[1] > 0) {
            int repeats = MathHelper.ceil(Math.max(blur[0], blur[1]));
            passes.accept(uniforms -> {
               uniforms.set("BlurConfig", builder -> builder
                       .putVec2(1F / screenWidth, 1F / screenHeight)
                       .putVec2(blur[0], blur[1])
                       .putInt(repeats));
            });
        }
    });
    UniformBinding DOF_CONFIG = register(Psychedelicraft.id("post/depth_of_field"), (tickDelta, screenWidth, screenHeight, passes) -> {
        var config = PsychedelicraftClient.getConfig();

        float zNear = 0.05F;
        float zFar = ShaderContext.viewDistace();

        float focalPointNear = config.dofFocalPointNear.get() / zFar;
        float focalPointFar = config.dofFocalPointFar.get() / zFar;
        float focalBlurFar = config.dofFocalBlurFar.get();
        float focalBlurNear = config.dofFocalBlurNear.get();

        float near = Math.min(focalPointNear, focalPointFar);
        float far = Math.max(focalPointNear, focalPointFar) * 0.9F;// TODO: These might be swapped. Far is sometimes nearer than near

        int maxDof = MathHelper.ceil(Math.max(focalBlurFar, focalBlurNear));

        for (int n = 0; n < maxDof; n++) {
            float curBlurNear = MathHelper.clamp(focalBlurNear - n, 0, 1);
            float curBlurFar = MathHelper.clamp(focalBlurFar - n, 0, 1);

            if (curBlurNear > 0 || curBlurFar > 0) {
                for (int i = 0; i < 2; i++) {
                    int vertical = i;
                    passes.accept(uniforms -> uniforms.set("DofConfig", builder -> builder
                            .putVec2(1F / screenWidth, 1F / screenHeight) // pixelSize
                            .putVec2(zNear, zFar)
                            .putVec2(near, far)
                            .putVec2(curBlurNear, curBlurFar)
                            .putInt(vertical)
                    ));
                }
            }
        }
    });
    UniformBinding BLOOM_CONFIG = register(Psychedelicraft.id("post/bloom"), (tickDelta, screenWidth, screenHeight, passes) -> {
        float bloom = ShaderContext.hallucinations().get(Drug.BLOOM_HALLUCINATION_STRENGTH);
        if (bloom > 0) {
            for (int n = 0; n < MathHelper.ceil(bloom); n++) {
                float totalAlpha = Math.min(1, bloom - n);
                for (int i = 0; i < 2; i++) {
                    int vertical = i;
                    passes.accept(uniforms -> uniforms.set("BloomConfig", builder -> builder
                            .putVec2(1F / screenWidth * 2F, 1F / screenHeight * 2F) // pixelSize
                            .putFloat(totalAlpha)
                            .putInt(vertical)));
                }
            }
        }
    });
    UniformBinding COLORED_BLOOM_CONFIG = register(Psychedelicraft.id("post/colored_bloom"), (tickDelta, screenWidth, screenHeight, passes) -> {
        Vector4fc color = ShaderContext.hallucinations().getColorBloom(tickDelta, RenderPhase.current() == RenderPhase.SKY);
        float alpha = color.w();
        if (alpha > 0) {
            for (int n = 0; n < MathHelper.ceil(alpha); n++) {
                float totalAlpha = Math.max(1, alpha - n);
                for (int i = 0; i < 2; i++) {
                    int vertical = i;
                    passes.accept(uniforms -> uniforms.set("ColoredBloomConfig", builder -> builder
                            .putVec2(1F / screenWidth, 1F / screenHeight) // pixelSize
                            .putVec4(color.x(), color.y(), color.z(), totalAlpha)
                            .putInt(vertical)
                    ));
                }
            }
        }
    });
    UniformBinding DOUBLE_VISION_CONFIG = register(Psychedelicraft.id("post/double_vision"), (tickDelta, screenWidth, screenHeight, passes) -> {
        float strength = ShaderContext.modifier(Drug.DOUBLE_VISION);

        if (strength > 0) {
            float distance = MathHelper.sin(ShaderContext.ticks() / 20F) * 0.05f * strength;
            passes.accept(uniforms -> uniforms.set("DoubleVisionConfig", builder -> builder
                    .putFloat(strength) // totalAlpha
                    .putFloat(distance)
                    .putFloat(1 + strength) // stretch
            ));
        }
    });
    UniformBinding BLUR_NOISE_CONFIG = register(Psychedelicraft.id("post/blur_noise"), (tickDelta, screenWidth, screenHeight, passes) -> {
        float strength = ShaderContext.drug(DrugType.POWER) * 0.6F;

        if (strength > 0) {
            float seed = RenderUtil.random((long) (ShaderContext.ticks() * 1000.0)).nextFloat() * 9 + 1;
            passes.accept(uniforms -> uniforms.set("BlurConfig", builder -> builder
                    .putVec2(1F / screenWidth, 1F / screenHeight) // pixelSize
                    .putFloat(strength) // totalAlpha
                    .putFloat(strength)
                    .putFloat(seed)
            ));
        }
    });
    UniformBinding DISTORTION_MAP_CONFIG = register(Psychedelicraft.id("post/distortion_map"), (tickDelta, screenWidth, screenHeight, passes) -> {
        float strength = DrugRenderer.INSTANCE.getEnvironmentalEffects().getWaterScreenDistortion();

        if (strength > 0) {
            var ticks = ShaderContext.ticks();
            passes.accept(uniforms -> uniforms.set("DistortionMapConfig", builder -> builder
                    .putFloat(strength) // totalAlpha
                    .putFloat(strength * 0.2F) // strength
                    .putVec4(0, ticks * 0.005F, 0.5F, ticks * 0.007F) // texTranslation
            ));
        }
    });
    UniformBinding DIGITAL_DEPTH_CONFIG = register(Psychedelicraft.id("post/digital_depth"), (tickDelta, screenWidth, screenHeight, passes) -> {
        float digital = ShaderContext.drug(DrugType.ZERO);
        if (digital > 0) {
            float[] maxDownscale = PsychedelicraftClient.getConfig().getDigitalEffectPixelResize();
            float downscale = MathUtils.mixEaseInOut(0, 0.95F, Math.min(digital * 3, 1)) + digital * 0.05f; //Bigger pixels!

            float textProgress = MathUtils.easeZeroToOne((digital - 0.2F) * 5);
            float binaryProgress = MathUtils.easeZeroToOne((digital - 0.8F) * 10);

            float maxColors = digital > 0.4F ? (Math.max(256F / ((digital - 0.4F) * 640 + 1), 2)) : -1;
            float saturation = 1 - MathUtils.easeZeroToOne((digital - 0.6F) * 5);

            passes.accept(uniforms -> uniforms.set("PixelationConfig", builder -> builder
                    .putVec2(
                            screenWidth * (1 + (maxDownscale[0] - 1) * downscale),
                            screenHeight * (1 + (maxDownscale[1] - 1) * downscale
                    )) // newResolution
                    .putFloat(textProgress + binaryProgress) // textProgress
                    .putFloat(maxColors)
                    .putFloat(saturation)
                    .putFloat(1) // totalAlpha
                    .putVec2(0.05F, ShaderContext.viewDistace()) // depthRange
            ));
        }
    });
}
