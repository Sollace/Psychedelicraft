package ivorius.psychedelicraft.mixin.client.shader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ivorius.psychedelicraft.client.render.shader.BuiltGemoetryShader;
import net.minecraft.client.gl.GlCommandEncoder;
import net.minecraft.client.gl.ShaderProgram;

@Mixin(GlCommandEncoder.class)
abstract class MixinGlCommandEncoder {
    @Shadow
    private ShaderProgram currentProgram;

    @Inject(method = "setupRenderPass", at = @At("RETURN"))
    private void onSetupRenderPass(CallbackInfoReturnable<Boolean> info) {
        if (info.getReturnValueZ() && currentProgram instanceof BuiltGemoetryShader.Holder holder && holder.getUniformData() != null) {
            holder.getUniformData().bind();
        }
    }
}
