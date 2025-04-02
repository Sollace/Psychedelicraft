package ivorius.psychedelicraft.mixin.client.sodium;

import java.util.function.Function;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.client.render.shader.BuiltGemoetryShader;
import ivorius.psychedelicraft.client.render.shader.GeometryShader;

@Pseudo
@Mixin(targets = {
        "net.caffeinemc.mods.sodium.client.gl.shader.GlProgram$Builder",
        "me.jellysquid.mods.sodium.client.gl.shader.GlProgram$Builder"
}, remap = false)
abstract class MixinGlProgram_Builder {
    @Unique
    private int psychedelicraft_maxAttributes = -1;
    @Unique
    private int psychedelicraft_maxFragments = -1;

    @Shadow
    private @Final int program;

    @Unique
    private @Nullable BuiltGemoetryShader.Builder psychedelicraft_shader;

    @Inject(method = "bindAttribute", at = @At("HEAD"))
    private void onBindAttribute(String name, int index, CallbackInfoReturnable<?> info) {
        psychedelicraft_maxAttributes = Math.max(psychedelicraft_maxAttributes, index);
    }

    @Inject(method = "bindFragmentData", at = @At("HEAD"))
    private void onBindFragmentData(String name, int index, CallbackInfoReturnable<?> info) {
        psychedelicraft_maxFragments = Math.max(psychedelicraft_maxFragments, index);
    }

    @Inject(method = "link", at = @At("HEAD"))
    private void onLink(Function<?, ?> factory, CallbackInfoReturnable<?> info) {
        if (PsychedelicraftClient.getConfig().sodiumSupport.get()) {
            psychedelicraft_shader = GeometryShader.INSTANCE.createShaderBuilder(program, psychedelicraft_maxAttributes, psychedelicraft_maxFragments);
        }
    }

    @Inject(method = "link", at = @At("RETURN"))
    private void afterLink(Function<?, ?> factory, CallbackInfoReturnable<?> info) {
        if (info.getReturnValue() instanceof BuiltGemoetryShader.Holder holder && psychedelicraft_shader != null) {
            holder.attachUniformData(psychedelicraft_shader.build());
        }
    }
}
