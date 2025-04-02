package ivorius.psychedelicraft.mixin.client.iris;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ivorius.psychedelicraft.client.IrisCompat;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.client.render.shader.GeometryShader;

@Pseudo
@Mixin(targets = "net.irisshaders.iris.uniforms.CommonUniforms", remap = false)
abstract class MixinCommonUniforms {
    @Inject(method = "generalCommonUniforms(Lnet/irisshaders/iris/gl/uniform/UniformHolder;Lnet/irisshaders/iris/uniforms/FrameUpdateNotifier;Lnet/irisshaders/iris/shaderpack/properties/PackDirectives;)V", at = @At("HEAD"))
    private static void onGeneralCommonUniforms(@Coerce Object uniforms, @Coerce Object updateNotifier, @Coerce Object directives, CallbackInfo info) {
        if (PsychedelicraftClient.getConfig().irisSupport.get()) {
            GeometryShader.INSTANCE.addUniforms(IrisCompat.wrapUniformHolder(uniforms));
        }
    }
}
