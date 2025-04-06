package ivorius.psychedelicraft.mixin.client.sodium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.client.render.shader.GeometryShader;
import net.minecraft.client.gl.ShaderStage;
import net.minecraft.util.Identifier;

@Pseudo
@Mixin(targets = {
        "net.caffeinemc.mods.sodium.client.gl.shader.ShaderLoader",
        "me.jellysquid.mods.sodium.client.gl.shader.ShaderLoader"
})
abstract class MixinShaderLoader {

    @Inject(method = "loadShader(Lnet/caffeinemc/mods/sodium/client/gl/shader/ShaderType;Lnet/minecraft/util/Identifier;Lnet/caffeinemc/mods/sodium/client/gl/shader/ShaderConstants;)Lnet/caffeinemc/mods/sodium/client/gl/shader/GlShader;", at = @At("HEAD"))
    private static void loadShader(@Coerce Object type, Identifier name, @Coerce Object constants, CallbackInfoReturnable<?> info) {
        if (PsychedelicraftClient.getConfig().sodiumSupport.get()) {
            GeometryShader.INSTANCE.setup(type.toString().contentEquals("VERTEX") ? ShaderStage.Type.VERTEX : ShaderStage.Type.FRAGMENT, name);
        }
    }

    @Inject(method = "getShaderSource(Lnet/minecraft/util/Identifier;)Ljava/lang/String;", at = @At("RETURN"), cancellable = true)
    private static void modifyShaderSources(Identifier name, CallbackInfoReturnable<String> info) {
        if (PsychedelicraftClient.getConfig().sodiumSupport.get()) {
            info.setReturnValue(GeometryShader.INSTANCE.injectShaderSources(info.getReturnValue()));
        }
    }
}
