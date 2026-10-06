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
@Mixin(targets = "net.caffeinemc.mods.sodium.client.gl.shader.GlProgram$Builder", remap = false)
abstract class MixinGlProgram_Builder {
    @Shadow
    private @Final int program;

    @Unique
    private @Nullable BuiltGemoetryShader.Builder psychedelicraft_shader;

    @Inject(method = "link", at = @At("RETURN"))
    private void afterLink(Function<?, ?> factory, CallbackInfoReturnable<?> info) {
        if (PsychedelicraftClient.getConfig().sodiumSupport.get() && info.getReturnValue() instanceof BuiltGemoetryShader.Holder holder) {
            holder.attachUniformData(GeometryShader.INSTANCE.getShaderBuilder().build(program));
        }
    }
}
