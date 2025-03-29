package ivorius.psychedelicraft.mixin.client.shader;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.blaze3d.shaders.ShaderType;

import ivorius.psychedelicraft.client.render.shader.GeometryShader;
import net.minecraft.client.gl.ShaderLoader;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

@Mixin(ShaderLoader.class)
abstract class MixinShaderLoader {
    @Inject(method = "loadShaderSource(Lnet/minecraft/util/Identifier;Lnet/minecraft/resource/Resource;Lnet/minecraft/client/gl/CompiledShader$Type;Ljava/util/Map;Lcom/google/common/collect/ImmutableMap$Builder;)V", at = @At("HEAD"))
    private static void onLoadShaderSource(Identifier id, Resource resource, ShaderType type, Map<Identifier, Resource> allResources, @SuppressWarnings("rawtypes") Builder builder, CallbackInfo info) {
        GeometryShader.INSTANCE.setup(type, type.idConverter().toResourceId(id));
    }
}