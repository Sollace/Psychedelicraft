package ivorius.psychedelicraft.mixin.client.sodium;

import java.util.function.Function;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ivorius.psychedelicraft.client.render.shader.BuiltGemoetryShader;
import ivorius.psychedelicraft.client.render.shader.GeometryShader;

@Pseudo
@Mixin(targets = {
        //"net.caffeinemc.mods.sodium.client.gl.shader.GlProgram$Builder",
        "me.jellysquid.mods.sodium.client.gl.shader.GlProgram$Builder"
})
abstract class MixinGlProgram_Builder {
    private int maxAttributes = -1;
    private int maxFragments = -1;

    @Shadow
    private @Final int program;

    private @Nullable BuiltGemoetryShader.Builder psychedelicraft_shader;

    @Inject(method = "bindAttribute", at = @At("HEAD"))
    private void onBindAttribute(String name, int index, CallbackInfoReturnable<?> info) {
        maxAttributes = Math.max(maxAttributes, index);
    }

    @Inject(method = "bindFragmentData", at = @At("HEAD"))
    private void onBindFragmentData(String name, int index, CallbackInfoReturnable<?> info) {
        maxFragments = Math.max(maxFragments, index);
    }

    @Inject(method = "link", at = @At("HEAD"))
    private void onLink(Function<?, ?> factory, CallbackInfoReturnable<?> info) {
        psychedelicraft_shader = GeometryShader.INSTANCE.createShaderBuilder(program, maxAttributes, maxFragments);
    }

    @Inject(method = "link", at = @At("RETURN"))
    private void afterLink(Function<?, ?> factory, CallbackInfoReturnable<?> info) {
        if (info.getReturnValue() instanceof BuiltGemoetryShader.Holder holder && psychedelicraft_shader != null) {
            holder.attachUniformData(psychedelicraft_shader.build());
        }
    }
}
