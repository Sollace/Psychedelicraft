package ivorius.psychedelicraft.mixin.client.sodium;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ivorius.psychedelicraft.client.render.shader.BuiltGemoetryShader;

@Pseudo
@Mixin(targets = {
        //"net.caffeinemc.mods.sodium.client.gl.shader.GlProgram",
        "me.jellysquid.mods.sodium.client.gl.shader.GlProgram"
})
abstract class MixinGlProgram implements BuiltGemoetryShader.Holder {
    private @Nullable BuiltGemoetryShader psychedelicraft_uniformData;

    @Inject(method = "bind()V", at = @At("RETURN"))
    private void onBind(CallbackInfo info) {
        if (psychedelicraft_uniformData != null) {
            psychedelicraft_uniformData.bind();
        }
    }

    @Override
    public void attachUniformData(@Nullable BuiltGemoetryShader uniformData) {
        psychedelicraft_uniformData = uniformData;
    }
}