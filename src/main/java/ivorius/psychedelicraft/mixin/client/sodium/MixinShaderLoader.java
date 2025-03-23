package ivorius.psychedelicraft.mixin.client.sodium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.shaders.ShaderType;

import ivorius.psychedelicraft.client.render.shader.GeometryShader;
import net.minecraft.util.Identifier;

@Pseudo
@Mixin(targets = {
        "net.caffeinemc.mods.sodium.client.gl.shader.ShaderLoader",
        "me.jellysquid.mods.sodium.client.gl.shader.ShaderLoader"
})
abstract class MixinShaderLoader {

    @Inject(method = "loadShader", at = @At("HEAD"))
    private static void loadShader(@Coerce Object type, Identifier name, @Coerce Object constants, CallbackInfoReturnable<?> info) {
        GeometryShader.INSTANCE.setup(type.toString().contentEquals("VERTEX") ? ShaderType.VERTEX : ShaderType.FRAGMENT, name);
    }

    @ModifyReturnValue(method = "getShaderSource(Lnet/minecraft/util/Identifier;)Ljava/lang/String;", at = @At("RETURN"))
    private static String modifyShaderSources(String sources, Identifier name) {
        return GeometryShader.INSTANCE.injectShaderSources(sources);
    }
}
